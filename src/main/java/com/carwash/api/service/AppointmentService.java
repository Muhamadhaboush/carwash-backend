package com.carwash.api.service;

import com.carwash.api.dto.request.appointment.CreateAppointmentRequest;
import com.carwash.api.dto.request.appointment.RescheduleAppointmentRequest;
import com.carwash.api.dto.request.appointment.UpdateAppointmentStatusRequest;
import com.carwash.api.dto.response.AppointmentResponse;
import com.carwash.api.dto.response.TimeSlotResponse;
import com.carwash.api.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {
    List<AppointmentResponse> getMyAppointments(String email);
    Page<AppointmentResponse> getMyAppointmentsPaged(String email, Pageable pageable);
    AppointmentResponse getMyAppointmentById(String email, Long appointmentId);
    Page<AppointmentResponse> getAllAppointmentsPaged(AppointmentStatus status, String yearMonth, String search, Pageable pageable);
    AppointmentResponse getAppointmentById(Long id);
    AppointmentResponse createAppointment(String email, CreateAppointmentRequest request);
    void cancelAppointment(String email, Long appointmentId);
    AppointmentResponse rescheduleAppointment(String email, Long appointmentId, RescheduleAppointmentRequest request);
    AppointmentResponse updateStatus(Long appointmentId, UpdateAppointmentStatusRequest request);
    List<TimeSlotResponse> getAvailableSlots(LocalDate date);
    AppointmentResponse markAsPaid(String email, Long appointmentId);
}
