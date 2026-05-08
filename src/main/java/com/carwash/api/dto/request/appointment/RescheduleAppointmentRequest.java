package com.carwash.api.dto.request.appointment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class RescheduleAppointmentRequest {

    @NotNull(message = "Yeni tarih boş olamaz")
    private LocalDate newDate;

    @NotNull(message = "Yeni saat boş olamaz")
    private LocalTime newTime;
}
