package com.carwash.api.dto.request.appointment;

import com.carwash.api.enums.DeliveryMethod;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateAppointmentRequest {

    @NotNull(message = "Hizmet ID boş olamaz")
    private Long serviceId;

    @NotNull(message = "Araç ID boş olamaz")
    private Long vehicleId;

    @NotNull(message = "Randevu tarihi boş olamaz")
    @FutureOrPresent(message = "Randevu tarihi geçmişte olamaz")
    private LocalDate appointmentDate;

    @NotNull(message = "Randevu saati boş olamaz")
    private LocalTime appointmentTime;

    @NotNull(message = "Teslimat yöntemi boş olamaz")
    private DeliveryMethod deliveryMethod;

    // Sadece VALET seçilirse zorunlu; service katmanında kontrol edilir
    private Long pickupAddressId;
}
