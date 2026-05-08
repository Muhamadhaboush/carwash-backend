package com.carwash.api.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "Mevcut şifre boş olamaz")
    private String currentPassword;

    @NotBlank(message = "Yeni şifre boş olamaz")
    @Size(min = 6, message = "Yeni şifre en az 6 karakter olmalıdır")
    private String newPassword;

    @NotBlank(message = "Şifre tekrarı boş olamaz")
    private String confirmNewPassword;
}
