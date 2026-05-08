package com.carwash.api.dto.request.settings;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BusinessSettingRequest {

    @NotBlank(message = "Anahtar boş olamaz")
    private String settingKey;

    private String settingValue;

    private String description;
}
