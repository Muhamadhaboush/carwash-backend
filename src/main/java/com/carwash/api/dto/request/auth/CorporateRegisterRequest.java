package com.carwash.api.dto.request.auth;

import com.carwash.api.dto.request.address.AddressRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CorporateRegisterRequest {

    @NotBlank(message = "Ad boş olamaz")
    private String firstName;

    @NotBlank(message = "Soyad boş olamaz")
    private String lastName;

    @NotBlank(message = "E-posta boş olamaz")
    @Email(message = "Geçerli bir e-posta giriniz")
    private String email;

    @NotBlank(message = "Şifre boş olamaz")
    @Size(min = 6, message = "Şifre en az 6 karakter olmalıdır")
    private String password;

    private String phone;

    @NotBlank(message = "Şirket adı boş olamaz")
    private String companyName;

    @NotBlank(message = "Vergi numarası boş olamaz")
    private String taxNumber;

    @Valid
    @NotNull(message = "Adres bilgisi zorunludur")
    private AddressRequest address;
}
