package com.carwash.api.service.impl;

import com.carwash.api.dto.request.appointment.CreateAppointmentRequest;
import com.carwash.api.dto.request.appointment.RescheduleAppointmentRequest;
import com.carwash.api.dto.request.appointment.UpdateAppointmentStatusRequest;
import com.carwash.api.dto.response.AppointmentResponse;
import com.carwash.api.dto.response.TimeSlotResponse;
import com.carwash.api.entity.*;
import com.carwash.api.enums.AppointmentStatus;
import com.carwash.api.enums.DeliveryMethod;
import com.carwash.api.exception.BusinessException;
import com.carwash.api.exception.ResourceNotFoundException;
import com.carwash.api.exception.UnauthorizedException;
import com.carwash.api.repository.*;
import com.carwash.api.service.AppointmentService;
import com.carwash.api.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final WashServiceRepository washServiceRepository;
    private final VehicleRepository vehicleRepository;
    private final AddressRepository addressRepository;
    private final ClosedDateRepository closedDateRepository;
    private final EmailService emailService;
    private final com.carwash.api.repository.CompanyServicePriceRepository companyPriceRepository;

    @Value("${app.appointment.slot.start-hour:9}")
    private int slotStartHour;

    @Value("${app.appointment.slot.end-hour:19}")
    private int slotEndHour;

    @Value("${app.appointment.slot.duration-minutes:30}")
    private int slotDurationMinutes;

    @Value("${app.appointment.slot.capacity:2}")
    private int slotCapacity;

    private static final List<AppointmentStatus> EXCLUDED_STATUSES =
            List.of(AppointmentStatus.CANCELLED, AppointmentStatus.REJECTED);

    // ── Müşteri: kendi randevuları (sayfalı) ──────────────────────
    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getMyAppointmentsPaged(String email, Pageable pageable) {
        User user = getUser(email);
        return appointmentRepository.findAllByUserId(user.getId(), pageable)
                .map(AppointmentResponse::from);
    }

    // ── Müşteri: kendi randevuları (liste - geriye dönük uyumluluk) ─
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyAppointments(String email) {
        User user = getUser(email);
        return appointmentRepository.findAllByUserId(user.getId())
                .stream()
                .sorted((a1, a2) -> {
                    int dateComp = a2.getAppointmentDate().compareTo(a1.getAppointmentDate());
                    if (dateComp != 0) return dateComp;
                    return a2.getAppointmentTime().compareTo(a1.getAppointmentTime());
                })
                .map(AppointmentResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getMyAppointmentById(String email, Long appointmentId) {
        User user = getUser(email);
        Appointment appointment = appointmentRepository.findByIdAndUserId(appointmentId, user.getId())
                .orElseThrow(() -> new UnauthorizedException("Bu randevuya erişim yetkiniz yok."));
        return AppointmentResponse.from(appointment);
    }

    // ── Admin: tüm randevular (sayfalı, ay filtreli) ───────────────────────
    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getAllAppointmentsPaged(AppointmentStatus status, String yearMonth, String search, Pageable pageable) {
        LocalDate yearStart = null;
        LocalDate yearEnd = null;
        if (yearMonth != null && !yearMonth.isBlank()) {
            try {
                String[] parts = yearMonth.split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                yearStart = LocalDate.of(year, month, 1);
                yearEnd = yearStart.withDayOfMonth(yearStart.lengthOfMonth());
            } catch (Exception ignored) {
                // invalid format, ignore month filter
            }
        }
        
        LocalDate finalYearStart = yearStart != null ? yearStart : LocalDate.of(1970, 1, 1);
        LocalDate finalYearEnd = yearEnd != null ? yearEnd : LocalDate.of(2100, 12, 31);
        
        String searchFilter = (search != null && !search.isBlank()) ? "%" + search.trim() + "%" : "";
        
        return appointmentRepository.findAllFiltered(status, finalYearStart, finalYearEnd, searchFilter, pageable)
                .map(AppointmentResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long id) {
        return AppointmentResponse.from(appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Randevu bulunamadı: " + id)));
    }

    // ── Randevu Oluştur ───────────────────────────────────────────
    @Override
    @Transactional
    public AppointmentResponse createAppointment(String email, CreateAppointmentRequest request) {
        User user = getUser(email);
        log.info("Randevu oluşturuluyor - kullanıcı: {}, tarih: {}, saat: {}",
                email, request.getAppointmentDate(), request.getAppointmentTime());

        WashService washService = washServiceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Hizmet bulunamadı: " + request.getServiceId()));
        if (!washService.isActive()) throw new BusinessException("Seçilen hizmet aktif değil.");

        // Geçmiş tarih + iş günü + kapalı gün validasyonu
        validateSlotFull(request.getAppointmentDate(), request.getAppointmentTime());

        // Kapasite kontrolü (hizmet süresi gözetilerek)
        checkCapacityForService(request.getAppointmentDate(), request.getAppointmentTime(), washService, null);

        Vehicle vehicle = vehicleRepository.findByIdAndUserId(request.getVehicleId(), user.getId())
                .orElseThrow(() -> new UnauthorizedException("Bu araca erişim yetkiniz yok."));

        // Aynı araç aynı gün çakışma kontrolü
        checkVehicleConflict(vehicle.getId(), request.getAppointmentDate(), null);

        Address pickupAddress = null;
        if (request.getDeliveryMethod() == DeliveryMethod.VALET) {
            if (request.getPickupAddressId() == null)
                throw new BusinessException("Vale seçeneği için adres bilgisi zorunludur.");
            pickupAddress = addressRepository.findByIdAndUserId(request.getPickupAddressId(), user.getId())
                    .orElseThrow(() -> new UnauthorizedException("Bu adrese erişim yetkiniz yok."));
        }

        boolean isCorporate = user.getUserType() == com.carwash.api.enums.UserType.CORPORATE;
        AppointmentStatus initialStatus = isCorporate ? AppointmentStatus.PENDING : AppointmentStatus.APPROVED;

        // Araç tipine göre özel fiyat varsa onu kullan, yoksa global fiyat
        java.math.BigDecimal finalPrice = washService.getPrice();
        if (washService.getVehiclePrices() != null && washService.getVehiclePrices().containsKey(vehicle.getVehicleType())) {
            finalPrice = washService.getVehiclePrices().get(vehicle.getVehicleType());
        }

        // Kurumsal kullanıcı için özel anlaşmalı fiyat varsa, her şeyi ezer
        if (isCorporate) {
            finalPrice = companyPriceRepository
                    .findByUserIdAndWashServiceId(user.getId(), washService.getId())
                    .map(p -> p.getCustomPrice())
                    .orElse(finalPrice);
        }

        Appointment appointment = Appointment.builder()
                .user(user)
                .service(washService)
                .vehicle(vehicle)
                .appointmentDate(request.getAppointmentDate())
                .appointmentTime(request.getAppointmentTime())
                .status(initialStatus)
                .deliveryMethod(request.getDeliveryMethod())
                .pickupAddress(pickupAddress)
                .totalPrice(finalPrice)
                .paymentRequired(!isCorporate)
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Randevu oluşturuldu - ID: {}", saved.getId());

        if (initialStatus == AppointmentStatus.APPROVED) {
            emailService.sendAppointmentConfirmedEmail(user, saved);
        } else {
            emailService.sendAppointmentCreatedEmail(user, saved);
            // Kurumsal randevu → admin'e bildirim gönder
            emailService.sendAdminNewAppointmentEmail(user, saved);
        }

        return AppointmentResponse.from(saved);
    }

    // ── Randevu İptal ─────────────────────────────────────────────
    @Override
    @Transactional
    public void cancelAppointment(String email, Long appointmentId) {
        User user = getUser(email);
        Appointment appointment = appointmentRepository.findByIdAndUserId(appointmentId, user.getId())
                .orElseThrow(() -> new UnauthorizedException("Bu randevuya erişim yetkiniz yok."));

        if (appointment.getStatus() != AppointmentStatus.PENDING &&
            appointment.getStatus() != AppointmentStatus.APPROVED) {
            throw new BusinessException("Sadece 'Onay Bekliyor' veya 'Onaylandı' durumundaki randevular iptal edilebilir.");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setAdminNote("Müşteri tarafından iptal edildi.");
        appointmentRepository.save(appointment);

        emailService.sendAppointmentCancelledEmail(user, appointment);
        log.info("Randevu iptal edildi - ID: {}, kullanıcı: {}", appointmentId, email);
    }

    // ── Ödeme Onayla ───────────────────────────────────────────────
    @Override
    @Transactional
    public AppointmentResponse markAsPaid(String email, Long appointmentId) {
        User user = getUser(email);
        Appointment appointment = appointmentRepository.findByIdAndUserId(appointmentId, user.getId())
                .orElseThrow(() -> new UnauthorizedException("Bu randevuya erişim yetkiniz yok."));

        if (!appointment.isPaymentRequired()) {
            throw new com.carwash.api.exception.BusinessException("Bu randevu için ödeme gerekmiyor.");
        }
        if (appointment.isPaid()) {
            throw new com.carwash.api.exception.BusinessException("Bu randevu zaten ödenmiş.");
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED ||
            appointment.getStatus() == AppointmentStatus.REJECTED) {
            throw new com.carwash.api.exception.BusinessException("İptal veya reddedilmiş randevu için ödeme yapılamaz.");
        }

        appointment.setIsPaid(true);
        Appointment saved = appointmentRepository.save(appointment);
        log.info("Ödeme kaydedildi - Randevu ID: {}, kullanıcı: {}", appointmentId, email);
        return AppointmentResponse.from(saved);
    }

    // ── Randevu Yeniden Planla ────────────────────────────────────
    @Override
    @Transactional
    public AppointmentResponse rescheduleAppointment(String email, Long appointmentId, RescheduleAppointmentRequest request) {
        User user = getUser(email);
        Appointment appointment = appointmentRepository.findByIdAndUserId(appointmentId, user.getId())
                .orElseThrow(() -> new UnauthorizedException("Bu randevuya erişim yetkiniz yok."));

        if (appointment.getStatus() != AppointmentStatus.PENDING &&
            appointment.getStatus() != AppointmentStatus.APPROVED) {
            throw new BusinessException("Sadece 'Onay Bekliyor' veya 'Onaylandı' durumundaki randevular yeniden planlanabilir.");
        }

        validateSlotFull(request.getNewDate(), request.getNewTime());
        checkCapacityForService(request.getNewDate(), request.getNewTime(), appointment.getService(), appointmentId);
        checkVehicleConflict(appointment.getVehicle().getId(), request.getNewDate(), appointmentId);

        boolean isCorporate = user.getUserType() == com.carwash.api.enums.UserType.CORPORATE;
        AppointmentStatus newStatus = isCorporate ? AppointmentStatus.PENDING : AppointmentStatus.APPROVED;

        appointment.setAppointmentDate(request.getNewDate());
        appointment.setAppointmentTime(request.getNewTime());
        appointment.setStatus(newStatus); 
        Appointment saved = appointmentRepository.save(appointment);

        if (newStatus == AppointmentStatus.APPROVED) {
            emailService.sendAppointmentConfirmedEmail(user, saved);
        } else {
            emailService.sendAppointmentRescheduledEmail(user, saved);
        }
        log.info("Randevu yeniden planlandı - ID: {}, yeni: {} {}", appointmentId, request.getNewDate(), request.getNewTime());

        return AppointmentResponse.from(saved);
    }

    // ── Admin: Durum Güncelle ─────────────────────────────────────
    @Override
    @Transactional
    public AppointmentResponse updateStatus(Long appointmentId, UpdateAppointmentStatusRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Randevu bulunamadı: " + appointmentId));

        AppointmentStatus oldStatus = appointment.getStatus();
        appointment.setStatus(request.getStatus());
        if (request.getAdminNote() != null) appointment.setAdminNote(request.getAdminNote());

        // Kurumsal müşteriler ödeme yapmaz, COMPLETED olunca ciroya girer
        if (request.getStatus() == AppointmentStatus.COMPLETED && !appointment.isPaid()) {
            appointment.setIsPaid(true);
        }

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Randevu durumu güncellendi - ID: {}, {} → {}", appointmentId, oldStatus, request.getStatus());

        // Durum geçişlerine göre mail gönder
        User user = saved.getUser();
        switch (request.getStatus()) {
            case APPROVED ->   emailService.sendAppointmentConfirmedEmail(user, saved);
            case REJECTED ->   emailService.sendAppointmentRejectedEmail(user, saved, request.getAdminNote());
            case COMPLETED ->  emailService.sendVehicleReadyEmail(user, saved);
            default -> { /* PENDING, CANCELLED için mail yok */ }
        }

        return AppointmentResponse.from(saved);
    }

    // ── Müsait Slotları Getir ─────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<TimeSlotResponse> getAvailableSlots(LocalDate date) {
        // Kapalı gün ise boş dön
        if (closedDateRepository.existsByClosedDate(date)) {
            return new ArrayList<>();
        }
        List<Appointment> validAppointments = appointmentRepository.findAllByAppointmentDate(date)
                .stream()
                .filter(a -> !EXCLUDED_STATUSES.contains(a.getStatus()))
                .collect(Collectors.toList());

        List<TimeSlotResponse> slots = new ArrayList<>();
        LocalTime current = LocalTime.of(slotStartHour, 0);
        LocalTime end = LocalTime.of(slotEndHour, 0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        while (current.isBefore(end)) {
            LocalTime loopTime = current;
            // Kaç aktif randevu bu slotta var?
            long count = validAppointments.stream()
                    .filter(a -> isSlotOccupied(a, loopTime))
                    .count();

            slots.add(TimeSlotResponse.builder()
                    .time(current)
                    .available(count < slotCapacity)
                    .label(current.format(formatter))
                    .build());
            current = current.plusMinutes(slotDurationMinutes);
        }

        return slots;
    }

    // ── Yardımcı: Kapasite + Hizmet Süresi Kontrolü ──────────────
    private void checkCapacityForService(LocalDate date, LocalTime time,
                                          WashService service, Long excludeAppointmentId) {
        List<Appointment> dayAppointments = appointmentRepository.findAllByAppointmentDate(date)
                .stream()
                .filter(a -> !EXCLUDED_STATUSES.contains(a.getStatus()))
                .filter(a -> excludeAppointmentId == null || !a.getId().equals(excludeAppointmentId))
                .collect(Collectors.toList());

        // Hizmetin kapladığı slotları hesapla
        int requiredSlots = (int) Math.ceil((double) service.getDurationMinutes() / slotDurationMinutes);

        for (int i = 0; i < requiredSlots; i++) {
            LocalTime checkTime = time.plusMinutes((long) i * slotDurationMinutes);
            if (!checkTime.isBefore(LocalTime.of(slotEndHour, 0))) {
                throw new BusinessException("Seçilen hizmet (" + service.getDurationMinutes() +
                        " dk) mesai saati dışına taşmaktadır.");
            }
            LocalTime ft = checkTime;
            long count = dayAppointments.stream()
                    .filter(a -> isSlotOccupied(a, ft))
                    .count();
            if (count >= slotCapacity) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
                throw new BusinessException("Seçilen saatte (" + checkTime.format(fmt) +
                        ") kapasite doldu. Başka bir saat seçin.");
            }
        }
    }

    /**
     * Bir randevunun verilen time slotunu işgal edip etmediğini kontrol eder.
     * Hizmet süresi boyunca işgal devam eder.
     */
    private boolean isSlotOccupied(Appointment a, LocalTime slotTime) {
        LocalTime start = a.getAppointmentTime();
        int duration = a.getService().getDurationMinutes();
        LocalTime finish = start.plusMinutes(duration);
        // slotTime, [start, finish) aralığındaysa slot işgal altında
        return !slotTime.isBefore(start) && slotTime.isBefore(finish);
    }

    // ── Yardımcı: Aynı Araç Aynı Gün Çakışma ────────────────────
    private void checkVehicleConflict(Long vehicleId, LocalDate date, Long excludeAppointmentId) {
        List<Appointment> existing = appointmentRepository
                .findAllByVehicleIdAndAppointmentDateAndStatusNotIn(vehicleId, date, EXCLUDED_STATUSES);

        boolean conflict = existing.stream()
                .anyMatch(a -> excludeAppointmentId == null || !a.getId().equals(excludeAppointmentId));

        if (conflict) {
            throw new BusinessException("Bu araç için " + date + " tarihinde zaten bir randevu mevcut.");
        }
    }

    // ── Yardımcı: Tam Slot Validasyonu ───────────────────────────
    private void validateSlotFull(LocalDate date, LocalTime time) {
        // Geçmiş tarih kontrolü
        if (date.isBefore(LocalDate.now())) {
            throw new BusinessException("Geçmiş tarihe randevu oluşturulamaz.");
        }
        // Bugünse geçmiş saat kontrolü
        if (date.isEqual(LocalDate.now()) && !time.isAfter(LocalTime.now())) {
            throw new BusinessException("Geçmiş saate randevu oluşturulamaz.");
        }
        // Pazar kontrolü kaldırıldı
        // Kapalı gün kontrolü
        if (closedDateRepository.existsByClosedDate(date)) {
            String reason = closedDateRepository.findByClosedDate(date)
                    .map(cd -> cd.getReason())
                    .orElse("Kapalı gün");
            throw new BusinessException("Seçilen tarih kapalı: " + reason);
        }
        // Mesai saati kontrolü
        LocalTime start = LocalTime.of(slotStartHour, 0);
        LocalTime end = LocalTime.of(slotEndHour, 0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        if (time.isBefore(start) || !time.isBefore(end)) {
            throw new BusinessException(String.format(
                    "Randevu saati %s-%s arasında olmalıdır.", start.format(fmt), end.format(fmt)));
        }
        // 30'ar dakika aralığı kontrolü
        if (time.getMinute() % slotDurationMinutes != 0) {
            throw new BusinessException("Randevu saati " + slotDurationMinutes + " dakika aralıklarında olmalıdır.");
        }
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı."));
    }
}