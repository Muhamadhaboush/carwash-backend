package com.carwash.api.dto.request.address;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressRequest {

    private String label; // "Ev", "İş"

    @NotBlank(message = "Sokak/cadde bilgisi boş olamaz")
    private String street;

    private String district;

    @NotBlank(message = "Şehir bilgisi boş olamaz")
    private String city;

    private String postalCode;

    private String country = "Türkiye";
}
