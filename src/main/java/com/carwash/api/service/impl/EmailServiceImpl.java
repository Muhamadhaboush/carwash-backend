package com.carwash.api.service.impl;

import com.carwash.api.entity.Appointment;
import com.carwash.api.entity.User;
import com.carwash.api.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:nygcarwash@gmail.com}")
    private String fromEmail;

    @Value("${app.business.name:Nyg Auto Garage}")
    private String businessName;

    @Value("${app.admin.notification.email:nygcarwash@gmail.com}")
    private String adminNotificationEmail;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // ─── Araç Hazır ───────────────────────────────────────────────
    @Override
    public void sendVehicleReadyEmail(User user, Appointment appointment) {
        String subject = "🚗 Aracınız Hazır! — " + businessName;
        String text = String.format(
                "Merhaba %s,\n\n" +
                "%s plakalı aracınızın yıkama işlemi tamamlandı. Aracınızı teslim alabilirsiniz.\n\n" +
                "📅 Randevu: %s %s\n" +
                "🔧 Hizmet: %s\n\n" +
                "Bizi tercih ettiğiniz için teşekkür ederiz.\n%s",
                user.getFirstName(),
                appointment.getVehicle().getPlateNumber(),
                appointment.getAppointmentDate().format(DATE_FMT),
                appointment.getAppointmentTime().format(TIME_FMT),
                appointment.getService().getName(),
                businessName);
        send(user.getEmail(), subject, text);
    }

    // ─── Randevu Oluşturuldu ──────────────────────────────────────
    @Override
    public void sendAppointmentCreatedEmail(User user, Appointment appointment) {
        String subject = "✅ Randevunuz Alındı — " + businessName;
        String text = String.format(
                "Merhaba %s,\n\n" +
                "Randevunuz başarıyla oluşturuldu. Onay beklemektedir.\n\n" +
                "📅 Tarih: %s\n" +
                "🕐 Saat: %s\n" +
                "🚗 Araç: %s %s (%s)\n" +
                "🔧 Hizmet: %s\n" +
                "💰 Tutar: %.2f ₺\n" +
                "🚚 Teslimat: %s\n\n" +
                "Randevunuz onaylandığında size bilgi vereceğiz.\n%s",
                user.getFirstName(),
                appointment.getAppointmentDate().format(DATE_FMT),
                appointment.getAppointmentTime().format(TIME_FMT),
                appointment.getVehicle().getBrand(),
                appointment.getVehicle().getModel(),
                appointment.getVehicle().getPlateNumber(),
                appointment.getService().getName(),
                appointment.getTotalPrice().doubleValue(),
                appointment.getDeliveryMethod() == com.carwash.api.enums.DeliveryMethod.VALET ? "Vale (Adresten Alınacak)" : "Kendiniz Getireceksiniz",
                businessName);
        send(user.getEmail(), subject, text);
    }

    // ─── Randevu Onaylandı ────────────────────────────────────────
    @Override
    public void sendAppointmentConfirmedEmail(User user, Appointment appointment) {
        String subject = "🎉 Randevunuz Onaylandı — " + businessName;
        String text = String.format(
                "Merhaba %s,\n\n" +
                "Randevunuz onaylandı!\n\n" +
                "📅 Tarih: %s\n" +
                "🕐 Saat: %s\n" +
                "🚗 Araç: %s %s (%s)\n" +
                "🔧 Hizmet: %s\n" +
                "💰 Tutar: %.2f ₺\n\n" +
                "%s" +
                "Sizi bekliyoruz!\n%s",
                user.getFirstName(),
                appointment.getAppointmentDate().format(DATE_FMT),
                appointment.getAppointmentTime().format(TIME_FMT),
                appointment.getVehicle().getBrand(),
                appointment.getVehicle().getModel(),
                appointment.getVehicle().getPlateNumber(),
                appointment.getService().getName(),
                appointment.getTotalPrice().doubleValue(),
                appointment.getAdminNote() != null ? "📝 Not: " + appointment.getAdminNote() + "\n\n" : "",
                businessName);
        send(user.getEmail(), subject, text);
    }

    // ─── Randevu Reddedildi ───────────────────────────────────────
    @Override
    public void sendAppointmentRejectedEmail(User user, Appointment appointment, String reason) {
        String subject = "❌ Randevunuz Onaylanamadı — " + businessName;
        String text = String.format(
                "Merhaba %s,\n\n" +
                "Maalesef %s tarihli %s saatli randevunuz onaylanamadı.\n\n" +
                "%s" +
                "Başka bir tarih veya saat için yeni randevu oluşturabilirsiniz.\n\n" +
                "Anlayışınız için teşekkür ederiz.\n%s",
                user.getFirstName(),
                appointment.getAppointmentDate().format(DATE_FMT),
                appointment.getAppointmentTime().format(TIME_FMT),
                reason != null ? "📝 Sebep: " + reason + "\n\n" : "",
                businessName);
        send(user.getEmail(), subject, text);
    }

    // ─── Randevu İptal Edildi ─────────────────────────────────────
    @Override
    public void sendAppointmentCancelledEmail(User user, Appointment appointment) {
        String subject = "🚫 Randevunuz İptal Edildi — " + businessName;
        String text = String.format(
                "Merhaba %s,\n\n" +
                "%s tarihli %s saatli randevunuz iptal edildi.\n\n" +
                "🚗 Araç: %s (%s)\n" +
                "🔧 Hizmet: %s\n\n" +
                "Yeni randevu almak için sitemizi ziyaret edebilirsiniz.\n%s",
                user.getFirstName(),
                appointment.getAppointmentDate().format(DATE_FMT),
                appointment.getAppointmentTime().format(TIME_FMT),
                appointment.getVehicle().getBrand() + " " + appointment.getVehicle().getModel(),
                appointment.getVehicle().getPlateNumber(),
                appointment.getService().getName(),
                businessName);
        send(user.getEmail(), subject, text);
    }

    // ─── Randevu Yeniden Planlandı ────────────────────────────────
    @Override
    public void sendAppointmentRescheduledEmail(User user, Appointment appointment) {
        String subject = "📅 Randevunuz Güncellendi — " + businessName;
        String text = String.format(
                "Merhaba %s,\n\n" +
                "Randevunuz yeni bir tarihe/saate alındı ve onay beklemektedir.\n\n" +
                "📅 Yeni Tarih: %s\n" +
                "🕐 Yeni Saat: %s\n" +
                "🚗 Araç: %s %s (%s)\n" +
                "🔧 Hizmet: %s\n\n" +
                "Onaylandığında tekrar bilgilendirileceksiniz.\n%s",
                user.getFirstName(),
                appointment.getAppointmentDate().format(DATE_FMT),
                appointment.getAppointmentTime().format(TIME_FMT),
                appointment.getVehicle().getBrand(),
                appointment.getVehicle().getModel(),
                appointment.getVehicle().getPlateNumber(),
                appointment.getService().getName(),
                businessName);
        send(user.getEmail(), subject, text);
    }

    // ─── Şifre Sıfırlama ──────────────────────────────────────────
    @Override
    public void sendPasswordResetEmail(User user, String resetLink) {
        String subject = "🔐 Şifre Sıfırlama — " + businessName;
        String text = String.format(
                "Merhaba %s,\n\n" +
                "Şifrenizi sıfırlamak için aşağıdaki bağlantıya tıklayın:\n\n" +
                "%s\n\n" +
                "Bu bağlantı 1 saat geçerlidir. Eğer bu isteği siz yapmadıysanız, bu e-postayı dikkate almayınız.\n\n" +
                "%s",
                user.getFirstName(),
                resetLink,
                businessName);
        send(user.getEmail(), subject, text);
    }

    // ─── Admin: Yeni Kurumsal Randevu Bildirimi ──────────────────
    @Override
    public void sendAdminNewAppointmentEmail(User customer, Appointment appointment) {
        String subject = "[YENI TALEP] Kurumsal Randevu — " + businessName;
        String text = String.format(
                "Yeni bir kurumsal randevu talebi geldi. Lutfen admin panelinden onaylayin veya reddedin.\n\n" +
                "── Musteri Bilgileri ──\n" +
                "Ad Soyad : %s %s\n" +
                "E-posta  : %s\n" +
                "Firma    : %s\n\n" +
                "── Randevu Detaylari ──\n" +
                "Tarih    : %s\n" +
                "Saat     : %s\n" +
                "Hizmet   : %s\n" +
                "Arac     : %s %s (%s)\n" +
                "Tutar    : %.2f TL\n" +
                "Teslimat : %s\n\n" +
                "Admin paneli: http://localhost:5173/admin/appointments",
                customer.getFirstName(), customer.getLastName(),
                customer.getEmail(),
                customer.getCompanyName() != null ? customer.getCompanyName() : "-",
                appointment.getAppointmentDate().format(DATE_FMT),
                appointment.getAppointmentTime().format(TIME_FMT),
                appointment.getService().getName(),
                appointment.getVehicle().getBrand(),
                appointment.getVehicle().getModel(),
                appointment.getVehicle().getPlateNumber(),
                appointment.getTotalPrice().doubleValue(),
                appointment.getDeliveryMethod() == com.carwash.api.enums.DeliveryMethod.VALET
                        ? "Vale (Adresten Alinacak)" : "Musteri Getirecek");
        send(adminNotificationEmail, subject, text);
    }

    // ─── Ortak gönderici ──────────────────────────────────────────
    private void send(String to, String subject, String text) {

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("✅ Mail gönderildi → {} | {}", to, subject);
        } catch (Exception e) {
            log.error("❌ Mail gönderilemedi → {} | Hata: {}", to, e.getMessage());
            // Mail gönderilemese bile ana akışı kesmiyoruz
        }
    }
}
