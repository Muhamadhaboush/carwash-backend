package com.carwash.api.controller;

import com.carwash.api.dto.response.DashboardStatsResponse;
import com.carwash.api.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Admin paneli istatistik ve metrikleri")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "[ADMIN] Günlük ve aylık özet istatistiklerini getir (Ciro, randevu sayıları vb.)")
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }
}
