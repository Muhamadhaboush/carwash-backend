package com.carwash.api.controller;

import com.carwash.api.entity.CompanyServicePrice;
import com.carwash.api.entity.User;
import com.carwash.api.entity.WashService;
import com.carwash.api.exception.ResourceNotFoundException;
import com.carwash.api.repository.CompanyServicePriceRepository;
import com.carwash.api.repository.UserRepository;
import com.carwash.api.repository.WashServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users/{userId}/prices")
public class CompanyPriceController {

    private final CompanyServicePriceRepository companyPriceRepository;
    private final UserRepository userRepository;
    private final WashServiceRepository washServiceRepository;

    /** Şirketin tüm özel fiyatlarını listele */
    @GetMapping
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getCompanyPrices(@PathVariable Long userId) {
        List<CompanyServicePrice> prices = companyPriceRepository.findAllByUserId(userId);
        List<Map<String, Object>> result = prices.stream().map(p -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", p.getId());
            map.put("serviceId", p.getWashService() != null ? p.getWashService().getId() : null);
            map.put("serviceName", p.getWashService() != null ? p.getWashService().getName() : "Unknown");
            map.put("globalPrice", p.getWashService() != null ? p.getWashService().getPrice() : java.math.BigDecimal.ZERO);
            map.put("customPrice", p.getCustomPrice());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /** Şirket için belirli bir servise özel fiyat ayarla / güncelle */
    @PutMapping("/{serviceId}")
    public ResponseEntity<Map<String, Object>> setPrice(
            @PathVariable Long userId,
            @PathVariable Long serviceId,
            @RequestBody Map<String, Object> body) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + userId));
        WashService service = washServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Hizmet bulunamadı: " + serviceId));

        BigDecimal customPrice = new BigDecimal(body.get("customPrice").toString());

        CompanyServicePrice price = companyPriceRepository
                .findByUserIdAndWashServiceId(userId, serviceId)
                .orElse(CompanyServicePrice.builder().user(user).washService(service).build());
        price.setCustomPrice(customPrice);
        companyPriceRepository.save(price);

        return ResponseEntity.ok(Map.of(
                "serviceId", serviceId,
                "serviceName", service.getName(),
                "globalPrice", service.getPrice(),
                "customPrice", customPrice
        ));
    }

    /** Özel fiyatı kaldır (global fiyata dön) */
    @DeleteMapping("/{serviceId}")
    public ResponseEntity<Void> removePrice(@PathVariable Long userId, @PathVariable Long serviceId) {
        companyPriceRepository.deleteByUserIdAndWashServiceId(userId, serviceId);
        return ResponseEntity.noContent().build();
    }
}
