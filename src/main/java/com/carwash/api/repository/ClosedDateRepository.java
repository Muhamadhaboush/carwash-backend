package com.carwash.api.repository;

import com.carwash.api.entity.ClosedDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClosedDateRepository extends JpaRepository<ClosedDate, Long> {
    boolean existsByClosedDate(LocalDate date);
    Optional<ClosedDate> findByClosedDate(LocalDate date);
    List<ClosedDate> findAllByClosedDateGreaterThanEqualOrderByClosedDateAsc(LocalDate from);
}
