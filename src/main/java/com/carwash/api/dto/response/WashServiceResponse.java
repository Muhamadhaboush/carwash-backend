package com.carwash.api.dto.response;

import com.carwash.api.entity.WashService;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class WashServiceResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationMinutes;
    private boolean isActive;
    private java.util.Map<com.carwash.api.enums.VehicleType, BigDecimal> vehiclePrices;
    private LocalDateTime createdAt;

    public static WashServiceResponse from(WashService service) {
        return WashServiceResponse.builder()
                .id(service.getId())
                .name(service.getName())
                .description(service.getDescription())
                .price(service.getPrice())
                .vehiclePrices(service.getVehiclePrices() != null ? new java.util.HashMap<>(service.getVehiclePrices()) : null)
                .durationMinutes(service.getDurationMinutes())
                .isActive(service.isActive())
                .createdAt(service.getCreatedAt())
                .build();
    }
}
