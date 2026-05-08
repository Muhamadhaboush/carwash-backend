package com.carwash.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "closed_dates")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClosedDate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "closed_date", nullable = false, unique = true)
    private LocalDate closedDate;

    @Column(nullable = false)
    private String reason; // "Bayram", "Bakım", "Özel Kapalı Gün" vs.
}
