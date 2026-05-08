package com.carwash.api.dto.response;

import com.carwash.api.entity.Appointment;
import com.carwash.api.enums.AppointmentStatus;
import com.carwash.api.enums.DeliveryMethod;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class AppointmentResponse {
    private Long id;
    private Long userId;
    private String userFullName;
    private Long serviceId;
    private String serviceName;
    private Long vehicleId;
    private String vehiclePlate;
    private String vehicleBrand;
    private String vehicleModel;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private AppointmentStatus status;
    private DeliveryMethod deliveryMethod;
    private AddressResponse pickupAddress;
    private BigDecimal totalPrice;
    private boolean paymentRequired;
    private boolean isPaid;
    private String adminNote;
    private LocalDateTime createdAt;

    public static AppointmentResponse from(Appointment appointment) {
        AppointmentResponse.AppointmentResponseBuilder builder = AppointmentResponse.builder()
                .id(appointment.getId())
                .userId(appointment.getUser().getId())
                .userFullName(appointment.getUser().getFirstName() + " " + appointment.getUser().getLastName())
                .serviceId(appointment.getService().getId())
                .serviceName(appointment.getService().getName())
                .vehicleId(appointment.getVehicle().getId())
                .vehiclePlate(appointment.getVehicle().getPlateNumber())
                .vehicleBrand(appointment.getVehicle().getBrand())
                .vehicleModel(appointment.getVehicle().getModel())
                .appointmentDate(appointment.getAppointmentDate())
                .appointmentTime(appointment.getAppointmentTime())
                .status(appointment.getStatus())
                .deliveryMethod(appointment.getDeliveryMethod())
                .totalPrice(appointment.getTotalPrice())
                .paymentRequired(appointment.isPaymentRequired())
                .isPaid(appointment.isPaid())
                .adminNote(appointment.getAdminNote())
                .createdAt(appointment.getCreatedAt());

        if (appointment.getPickupAddress() != null) {
            builder.pickupAddress(AddressResponse.from(appointment.getPickupAddress()));
        }

        return builder.build();
    }
}
