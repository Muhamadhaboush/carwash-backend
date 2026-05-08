package com.carwash.api.controller;

import com.carwash.api.dto.request.appointment.CreateAppointmentRequest;
import com.carwash.api.dto.request.appointment.RescheduleAppointmentRequest;
import com.carwash.api.dto.request.appointment.UpdateAppointmentStatusRequest;
import com.carwash.api.dto.response.AppointmentResponse;
import com.carwash.api.dto.response.TimeSlotResponse;
import com.carwash.api.enums.AppointmentStatus;
import com.carwash.api.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Randevu", description = "Randevu oluşturma, listeleme, yeniden planlama ve durum yönetimi")
public class AppointmentController {

    private final AppointmentService appointmentService;

    // ── Müşteri endpoint'leri ──────────────────────────────────────

    @Operation(summary = "Müsait saat slotlarını listele (08:00-20:00, 30 dk aralıkla)")
    @GetMapping("/appointments/available-slots")
    public ResponseEntity<List<TimeSlotResponse>> getAvailableSlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(appointmentService.getAvailableSlots(date));
    }

    @Operation(summary = "Yeni randevu oluştur")
    @PostMapping("/appointments")
    public ResponseEntity<AppointmentResponse> createAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateAppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.createAppointment(userDetails.getUsername(), request));
    }

    @Operation(summary = "Kendi randevularımı listele (sayfalı)")
    @GetMapping("/appointments/my")
    public ResponseEntity<Page<AppointmentResponse>> getMyAppointments(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10, sort = {"appointmentDate", "appointmentTime"}, direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(appointmentService.getMyAppointmentsPaged(userDetails.getUsername(), pageable));
    }

    @Operation(summary = "Randevu detayını getir")
    @GetMapping("/appointments/my/{id}")
    public ResponseEntity<AppointmentResponse> getMyAppointmentById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getMyAppointmentById(userDetails.getUsername(), id));
    }

    @Operation(summary = "Randevuyu iptal et — CANCELLED olarak işaretlenir, mail gönderilir")
    @DeleteMapping("/appointments/my/{id}")
    public ResponseEntity<Void> cancelAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        appointmentService.cancelAppointment(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Bireysel müşteri ödemesini onayla — isPaid=true yapılır, ciroya eklenir")
    @PostMapping("/appointments/my/{id}/pay")
    public ResponseEntity<AppointmentResponse> markAsPaid(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.markAsPaid(userDetails.getUsername(), id));
    }

    @Operation(summary = "Randevuyu yeniden planla — yeni tarih/saat seçilir, tekrar PENDING'e döner")
    @PatchMapping("/appointments/my/{id}/reschedule")
    public ResponseEntity<AppointmentResponse> rescheduleAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody RescheduleAppointmentRequest request) {
        return ResponseEntity.ok(
                appointmentService.rescheduleAppointment(userDetails.getUsername(), id, request));
    }

    // ── Admin endpoint'leri ────────────────────────────────────────

    @Operation(summary = "[ADMIN] Tüm randevuları listele — sayfalı, durum, ay ve isim filtresi")
    @GetMapping("/admin/appointments")
    public ResponseEntity<Page<AppointmentResponse>> getAllAppointments(
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) String yearMonth,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = {"appointmentDate", "appointmentTime"}, direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(appointmentService.getAllAppointmentsPaged(status, yearMonth, search, pageable));
    }

    @Operation(summary = "[ADMIN] Randevu detayını getir")
    @GetMapping("/admin/appointments/{id}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    @Operation(summary = "[ADMIN] Randevu durumunu güncelle — APPROVED/REJECTED/COMPLETED, mail tetiklenir")
    @PatchMapping("/admin/appointments/{id}/status")
    public ResponseEntity<AppointmentResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAppointmentStatusRequest request) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, request));
    }
}
