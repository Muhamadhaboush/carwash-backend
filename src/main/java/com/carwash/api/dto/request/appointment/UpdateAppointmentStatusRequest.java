package com.carwash.api.dto.request.appointment;

import com.carwash.api.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAppointmentStatusRequest {

    @NotNull(message = "Durum boş olamaz")
    private AppointmentStatus status;

    private String adminNote;
}
