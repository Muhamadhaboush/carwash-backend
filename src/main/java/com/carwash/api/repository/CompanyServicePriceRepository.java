package com.carwash.api.repository;

import com.carwash.api.entity.CompanyServicePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompanyServicePriceRepository extends JpaRepository<CompanyServicePrice, Long> {
    
    @Query("SELECT p FROM CompanyServicePrice p JOIN FETCH p.washService WHERE p.user.id = :userId")
    List<CompanyServicePrice> findAllByUserId(@Param("userId") Long userId);
    
    Optional<CompanyServicePrice> findByUserIdAndWashServiceId(Long userId, Long washServiceId);
    
    void deleteByUserIdAndWashServiceId(Long userId, Long washServiceId);
}
