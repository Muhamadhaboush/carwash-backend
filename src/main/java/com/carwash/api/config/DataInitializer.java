package com.carwash.api.config;

import com.carwash.api.entity.User;
import com.carwash.api.entity.WashService;
import com.carwash.api.enums.Role;
import com.carwash.api.enums.UserType;
import com.carwash.api.repository.UserRepository;
import com.carwash.api.repository.WashServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final WashServiceRepository washServiceRepository;
    private final com.carwash.api.repository.BusinessSettingRepository businessSettingRepository;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Value("${app.admin.email:admin@carwash.com}")
    private String adminEmail;

    @Value("${app.admin.password:Admin123!}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        try {
            jdbcTemplate.execute("ALTER TABLE appointments DROP CONSTRAINT appointments_status_check;");
            log.info("✅ Dropped appointments_status_check constraint to allow CANCELLED status.");
        } catch (Exception e) {
            log.info("Constraint drop ignored (already dropped or doesn't exist).");
        }
        createAdminIfNotExists();
        createDefaultServicesIfEmpty();
        createDefaultSettingsIfEmpty();
    }

    private void createAdminIfNotExists() {
        if (userRepository.existsByEmail(adminEmail)) {
            log.info("✅ Admin kullanıcısı zaten mevcut: {}", adminEmail);
            return;
        }
        User admin = User.builder()
                .firstName("Admin")
                .lastName("Carwash")
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .phone("05001234567")
                .role(Role.ADMIN)
                .userType(UserType.INDIVIDUAL)
                .isActive(true)
                .build();
        userRepository.save(admin);
        log.info("🔑 Admin kullanıcısı oluşturuldu: {}", adminEmail);
    }

    private void createDefaultServicesIfEmpty() {
        if (washServiceRepository.count() > 0) {
            log.info("✅ Yıkama hizmetleri zaten mevcut, seed atlandı.");
            return;
        }
        List<WashService> services = List.of(
            WashService.builder().name("Dış Yıkama").description("Aracın dış yüzeyi basınçlı su ve köpükle yıkanır. Jantlar ve lastikler dahil.").price(new BigDecimal("120.00")).durationMinutes(20).isActive(true).build(),
            WashService.builder().name("İç Temizlik").description("Araç içi elektrikli süpürge ile temizlenir, torpido ve kapı panelleri silinir.").price(new BigDecimal("180.00")).durationMinutes(30).isActive(true).build(),
            WashService.builder().name("İç + Dış Yıkama").description("Dış yıkama ve iç temizlik birlikte uygulanır. En çok tercih edilen paket.").price(new BigDecimal("280.00")).durationMinutes(45).isActive(true).build(),
            WashService.builder().name("Detaylı Temizlik (Detailing)").description("İç+Dış yıkamaya ek olarak koltuk şampuanı, cam temizliği ve plastik parça bakımı uygulanır.").price(new BigDecimal("650.00")).durationMinutes(90).isActive(true).build(),
            WashService.builder().name("Motor Yıkama").description("Motor bölmesi buharlı temizlik yöntemiyle yıkanır. Yağ ve kir birikintileri giderilir.").price(new BigDecimal("250.00")).durationMinutes(30).isActive(true).build(),
            WashService.builder().name("Pasta & Cila").description("Araç lakesi üzerindeki ince çizikler pasta ile giderilir, ardından uzun süreli koruma için cila uygulanır.").price(new BigDecimal("1200.00")).durationMinutes(120).isActive(true).build(),
            WashService.builder().name("Seramik Kaplama").description("Aracın lakesine nano-seramik kaplama uygulanır. 2 yıla kadar koruma sağlar, su ve kiri iter.").price(new BigDecimal("3500.00")).durationMinutes(180).isActive(true).build()
        );
        washServiceRepository.saveAll(services);
        log.info("🚗 {} adet varsayılan yıkama hizmeti oluşturuldu.", services.size());
    }

    private void createDefaultSettingsIfEmpty() {
        if (businessSettingRepository.count() > 0) return;
        
        List<com.carwash.api.entity.BusinessSetting> settings = List.of(
            com.carwash.api.entity.BusinessSetting.builder().settingKey("businessOpenTime").settingValue("08:00").description("İşletme açılış saati").build(),
            com.carwash.api.entity.BusinessSetting.builder().settingKey("businessCloseTime").settingValue("20:00").description("İşletme kapanış saati").build(),
            com.carwash.api.entity.BusinessSetting.builder().settingKey("concurrentSlots").settingValue("2").description("Aynı anda yıkanabilen araç kapasitesi").build()
        );
        businessSettingRepository.saveAll(settings);
        log.info("⚙️ Varsayılan işletme ayarları oluşturuldu.");
    }
}
