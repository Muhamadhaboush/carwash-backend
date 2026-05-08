package com.carwash.api.dto.request.service;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WashServiceRequest {

    @NotBlank(message = "Hizmet adı boş olamaz")
    private String name;

    private String description;

    @NotNull(message = "Fiyat boş olamaz")
    @DecimalMin(value = "0.0", inclusive = false, message = "Fiyat 0'dan büyük olmalıdır")
    private BigDecimal price;

    @Positive(message = "Süre pozitif olmalıdır")
    private Integer durationMinutes = 30;

    private java.util.Map<com.carwash.api.enums.VehicleType, BigDecimal> vehiclePrices;
}
