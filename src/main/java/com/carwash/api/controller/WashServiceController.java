package com.carwash.api.controller;

import com.carwash.api.dto.request.service.WashServiceRequest;
import com.carwash.api.dto.response.WashServiceResponse;
import com.carwash.api.entity.CompanyServicePrice;
import com.carwash.api.repository.CompanyServicePriceRepository;
import com.carwash.api.repository.UserRepository;
import com.carwash.api.service.WashServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class WashServiceController {

    private final WashServiceService washServiceService;
    private final CompanyServicePriceRepository companyPriceRepository;
    private final UserRepository userRepository;

    // Herkese açık (public) — kurumsal kullanıcıysa özel fiyatı göster
    @GetMapping("/services")
    public ResponseEntity<List<WashServiceResponse>> getActiveServices(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<WashServiceResponse> services = washServiceService.getActiveServices();
        if (userDetails != null) {
            userRepository.findByEmail(userDetails.getUsername()).ifPresent(user -> {
                if (user.getUserType() == com.carwash.api.enums.UserType.CORPORATE) {
                    services.forEach(srv -> {
                        companyPriceRepository
                                .findByUserIdAndWashServiceId(user.getId(), srv.getId())
                                .ifPresent(cp -> srv.setPrice(cp.getCustomPrice()));
                    });
                }
            });
        }
        return ResponseEntity.ok(services);
    }

    @GetMapping("/services/{id}")
    public ResponseEntity<WashServiceResponse> getServiceById(@PathVariable Long id) {
        return ResponseEntity.ok(washServiceService.getServiceById(id));
    }

    // Admin
    @GetMapping("/admin/services")
    public ResponseEntity<List<WashServiceResponse>> getAllServices() {
        return ResponseEntity.ok(washServiceService.getAllServices());
    }

    @PostMapping("/admin/services")
    public ResponseEntity<WashServiceResponse> createService(@Valid @RequestBody WashServiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(washServiceService.createService(request));
    }

    @PutMapping("/admin/services/{id}")
    public ResponseEntity<WashServiceResponse> updateService(
            @PathVariable Long id,
            @Valid @RequestBody WashServiceRequest request) {
        return ResponseEntity.ok(washServiceService.updateService(id, request));
    }

    @PatchMapping("/admin/services/{id}/status")
    public ResponseEntity<WashServiceResponse> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(washServiceService.toggleStatus(id));
    }

    @DeleteMapping("/admin/services/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        washServiceService.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}
