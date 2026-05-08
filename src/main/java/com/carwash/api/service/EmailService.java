package com.carwash.api.service;

import com.carwash.api.entity.Appointment;
import com.carwash.api.entity.User;

public interface EmailService {
    void sendVehicleReadyEmail(User user, Appointment appointment);
    void sendAppointmentConfirmedEmail(User user, Appointment appointment);
    void sendAppointmentRejectedEmail(User user, Appointment appointment, String reason);
    void sendAppointmentCreatedEmail(User user, Appointment appointment);
    void sendAppointmentCancelledEmail(User user, Appointment appointment);
    void sendAppointmentRescheduledEmail(User user, Appointment appointment);
    /** Kurumsal randevu geldiginde admin'e bildirim gonder */
    void sendAdminNewAppointmentEmail(User customer, Appointment appointment);
    /** Sifre sifirlama linki gonder */
    void sendPasswordResetEmail(User user, String resetLink);
}
