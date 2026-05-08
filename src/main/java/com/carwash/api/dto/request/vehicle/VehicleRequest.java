package com.carwash.api.dto.request.vehicle;

import com.carwash.api.enums.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VehicleRequest {

    @NotBlank(message = "Plaka boş olamaz")
    private String plateNumber;

    @NotBlank(message = "Marka boş olamaz")
    private String brand;

    @NotBlank(message = "Model boş olamaz")
    private String model;

    @NotNull(message = "Araç tipi boş olamaz")
    private VehicleType vehicleType;
}
