package com.carwash.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "company_service_prices",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "wash_service_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyServicePrice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wash_service_id", nullable = false)
    private WashService washService;

    @Column(name = "custom_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal customPrice;
}
