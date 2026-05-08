package com.carwash.api.controller;

import com.carwash.api.dto.request.settings.BusinessSettingRequest;
import com.carwash.api.dto.response.BusinessSettingResponse;
import com.carwash.api.service.BusinessSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BusinessSettingController {

    private final BusinessSettingService businessSettingService;

    // Herkese açık
    @GetMapping("/settings")
    public ResponseEntity<List<BusinessSettingResponse>> getAllSettings() {
        return ResponseEntity.ok(businessSettingService.getAllSettings());
    }

    @GetMapping("/settings/{key}")
    public ResponseEntity<BusinessSettingResponse> getSettingByKey(@PathVariable String key) {
        return ResponseEntity.ok(businessSettingService.getSettingByKey(key));
    }

    // Admin
    @PostMapping("/admin/settings")
    public ResponseEntity<BusinessSettingResponse> createSetting(@Valid @RequestBody BusinessSettingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(businessSettingService.createSetting(request));
    }

    @PutMapping("/admin/settings/{key}")
    public ResponseEntity<BusinessSettingResponse> updateSetting(
            @PathVariable String key,
            @RequestBody BusinessSettingRequest request) {
        return ResponseEntity.ok(businessSettingService.updateSettingByKey(key, request));
    }
}
