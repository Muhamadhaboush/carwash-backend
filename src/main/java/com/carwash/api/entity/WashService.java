package com.carwash.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wash_services")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WashService extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "duration_minutes", nullable = false)
    @Builder.Default
    private Integer durationMinutes = 30;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @OneToMany(mappedBy = "service", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Appointment> appointments = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "wash_service_prices", joinColumns = @JoinColumn(name = "wash_service_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "vehicle_type")
    @Column(name = "price", precision = 10, scale = 2)
    @Builder.Default
    private java.util.Map<com.carwash.api.enums.VehicleType, BigDecimal> vehiclePrices = new java.util.HashMap<>();
}
