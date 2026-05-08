package com.carwash.api.repository;

import com.carwash.api.entity.WashService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WashServiceRepository extends JpaRepository<WashService, Long> {
    List<WashService> findAllByIsActiveTrue();
}
