package com.carwash.api.service.impl;

import com.carwash.api.dto.response.DashboardStatsResponse;
import com.carwash.api.enums.AppointmentStatus;
import com.carwash.api.repository.AppointmentRepository;
import com.carwash.api.repository.UserRepository;
import com.carwash.api.repository.WashServiceRepository;
import com.carwash.api.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final WashServiceRepository washServiceRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        LocalDate startOfLastMonth = today.minusMonths(1).withDayOfMonth(1);
        LocalDate endOfLastMonth = today.minusMonths(1).withDayOfMonth(today.minusMonths(1).lengthOfMonth());

        List<AppointmentStatus> excludeForPending = List.of(
                AppointmentStatus.CANCELLED, AppointmentStatus.REJECTED, AppointmentStatus.COMPLETED);
        List<AppointmentStatus> excludeForCompleted = List.of(
                AppointmentStatus.PENDING, AppointmentStatus.APPROVED,
                AppointmentStatus.CANCELLED, AppointmentStatus.REJECTED);

        // En çok tercih edilen hizmetler
        List<Object[]> topServicesRaw = appointmentRepository.findTopServicesBetweenDates(startOfMonth, endOfMonth);
        List<DashboardStatsResponse.ServiceStatItem> topServices = topServicesRaw.stream()
                .map(row -> DashboardStatsResponse.ServiceStatItem.builder()
                        .serviceName((String) row[0])
                        .count(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());

        // Araç tipi dağılımı
        List<Object[]> vehicleDistRaw = appointmentRepository.findVehicleTypeDistribution(startOfMonth, endOfMonth);
        Map<String, Long> vehicleTypeDistribution = new LinkedHashMap<>();
        vehicleDistRaw.forEach(row -> vehicleTypeDistribution.put(row[0].toString(), ((Number) row[1]).longValue()));

        return DashboardStatsResponse.builder()
                // Ciro
                .todayRevenue(appointmentRepository.sumRevenueByDate(today))
                .monthRevenue(appointmentRepository.sumRevenueBetweenDates(startOfMonth, endOfMonth))
                .lastMonthRevenue(appointmentRepository.sumRevenueBetweenDates(startOfLastMonth, endOfLastMonth))
                // Bugünkü randevu sayıları
                .pendingAppointmentsToday(appointmentRepository
                        .countByAppointmentDateAndStatusNotIn(today, excludeForPending))
                .completedAppointmentsToday(appointmentRepository
                        .countByAppointmentDateAndStatusNotIn(today, excludeForCompleted))
                .cancelledAppointmentsToday(appointmentRepository
                        .countByAppointmentDateAndStatus(today, AppointmentStatus.CANCELLED))
                .approvedAppointmentsToday(appointmentRepository
                        .countByAppointmentDateAndStatus(today, AppointmentStatus.APPROVED))
                // Bu ayki toplam
                .totalAppointmentsThisMonth(appointmentRepository
                        .countByAppointmentDateBetweenAndStatusNotIn(startOfMonth, endOfMonth,
                                List.of(AppointmentStatus.CANCELLED, AppointmentStatus.REJECTED)))
                // Genel
                .totalUsers((int) userRepository.count())
                .activeWashServices(washServiceRepository.findAllByIsActiveTrue().size())
                // Analizler
                .topServices(topServices)
                .vehicleTypeDistribution(vehicleTypeDistribution)
                .build();
    }
}
