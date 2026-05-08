package com.carwash.api.dto.response;

import com.carwash.api.entity.Vehicle;
import com.carwash.api.enums.VehicleType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VehicleResponse {
    private Long id;
    private String plateNumber;
    private String brand;
    private String model;
    private VehicleType vehicleType;
    private boolean isDefault;

    public static VehicleResponse from(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .plateNumber(vehicle.getPlateNumber())
                .brand(vehicle.getBrand())
                .model(vehicle.getModel())
                .vehicleType(vehicle.getVehicleType())
                .isDefault(vehicle.isDefault())
                .build();
    }
}
