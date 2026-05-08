package com.carwash.api.repository;

import com.carwash.api.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findAllByUserId(Long userId);
    Optional<Vehicle> findByIdAndUserId(Long id, Long userId);
    Optional<Vehicle> findByUserIdAndIsDefaultTrue(Long userId);
    boolean existsByUserIdAndPlateNumber(Long userId, String plateNumber);
}
