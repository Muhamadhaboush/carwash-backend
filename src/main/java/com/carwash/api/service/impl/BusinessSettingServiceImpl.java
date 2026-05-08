package com.carwash.api.service.impl;

import com.carwash.api.dto.request.settings.BusinessSettingRequest;
import com.carwash.api.dto.response.BusinessSettingResponse;
import com.carwash.api.entity.BusinessSetting;
import com.carwash.api.exception.BusinessException;
import com.carwash.api.exception.ResourceNotFoundException;
import com.carwash.api.repository.BusinessSettingRepository;
import com.carwash.api.service.BusinessSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessSettingServiceImpl implements BusinessSettingService {

    private final BusinessSettingRepository businessSettingRepository;

    @Override
    public List<BusinessSettingResponse> getAllSettings() {
        return businessSettingRepository.findAll()
                .stream().map(BusinessSettingResponse::from).collect(Collectors.toList());
    }

    @Override
    public BusinessSettingResponse getSettingByKey(String key) {
        return BusinessSettingResponse.from(businessSettingRepository.findBySettingKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Ayar bulunamadı: " + key)));
    }

    @Override
    @Transactional
    public BusinessSettingResponse createSetting(BusinessSettingRequest request) {
        if (businessSettingRepository.existsBySettingKey(request.getSettingKey()))
            throw new BusinessException("Bu anahtar zaten mevcut: " + request.getSettingKey());
        BusinessSetting setting = BusinessSetting.builder()
                .settingKey(request.getSettingKey())
                .settingValue(request.getSettingValue())
                .description(request.getDescription())
                .build();
        return BusinessSettingResponse.from(businessSettingRepository.save(setting));
    }

    @Override
    @Transactional
    public BusinessSettingResponse updateSettingByKey(String key, BusinessSettingRequest request) {
        BusinessSetting setting = businessSettingRepository.findBySettingKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Ayar bulunamadı: " + key));
        setting.setSettingValue(request.getSettingValue());
        if (request.getDescription() != null) setting.setDescription(request.getDescription());
        return BusinessSettingResponse.from(businessSettingRepository.save(setting));
    }
}

