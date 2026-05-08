package com.carwash.api.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @NotBlank(message = "Ad boş olamaz")
    private String firstName;

    @NotBlank(message = "Soyad boş olamaz")
    private String lastName;

    private String phone;

    // Kurumsal kullanıcılar için
    private String companyName;
    private String taxNumber;
}
