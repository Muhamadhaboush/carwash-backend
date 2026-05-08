package com.carwash.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("🚗 Otoyıkama Randevu Sistemi API")
                        .description("""
                                ## Otoyıkama Randevu Sistemi - REST API
                                
                                Bu API, otoyıkama işletmesi için müşteri kayıt, araç yönetimi,
                                randevu alma ve admin yönetim işlemlerini kapsamaktadır.
                                
                                ### Kullanım
                                1. `/auth/register/individual` veya `/auth/register/corporate` ile kayıt olun
                                2. `/auth/login` ile giriş yapın ve `accessToken` alın
                                3. Sağ üstteki **Authorize** butonuna tıklayın ve `Bearer <token>` girin
                                4. Tüm korumalı endpoint'leri kullanabilirsiniz
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Carwash API Team")
                                .email("admin@carwash.com")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token giriniz. Örnek: Bearer eyJhbGci...")));
    }
}
