package com.carwash.api.service.impl;

import com.carwash.api.dto.request.service.WashServiceRequest;
import com.carwash.api.dto.response.WashServiceResponse;
import com.carwash.api.entity.WashService;
import com.carwash.api.exception.ResourceNotFoundException;
import com.carwash.api.repository.WashServiceRepository;
import com.carwash.api.service.WashServiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WashServiceServiceImpl implements WashServiceService {

    private final WashServiceRepository washServiceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<WashServiceResponse> getActiveServices() {
        return washServiceRepository.findAllByIsActiveTrue()
                .stream().map(WashServiceResponse::from).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WashServiceResponse> getAllServices() {
        return washServiceRepository.findAll()
                .stream().map(WashServiceResponse::from).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WashServiceResponse getServiceById(Long id) {
        return WashServiceResponse.from(findById(id));
    }

    @Override
    @Transactional
    public WashServiceResponse createService(WashServiceRequest request) {
        WashService service = WashService.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .durationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 30)
                .vehiclePrices(request.getVehiclePrices() != null ? request.getVehiclePrices() : new java.util.HashMap<>())
                .isActive(true)
                .build();
        return WashServiceResponse.from(washServiceRepository.save(service));
    }

    @Override
    @Transactional
    public WashServiceResponse updateService(Long id, WashServiceRequest request) {
        WashService service = findById(id);
        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setPrice(request.getPrice());
        if (request.getVehiclePrices() != null) {
            service.setVehiclePrices(request.getVehiclePrices());
        }
        if (request.getDurationMinutes() != null) service.setDurationMinutes(request.getDurationMinutes());
        return WashServiceResponse.from(washServiceRepository.save(service));
    }

    @Override
    @Transactional
    public WashServiceResponse toggleStatus(Long id) {
        WashService service = findById(id);
        service.setActive(!service.isActive());
        return WashServiceResponse.from(washServiceRepository.save(service));
    }

    @Override
    @Transactional
    public void deleteService(Long id) {
        WashService service = findById(id);
        try {
            washServiceRepository.delete(service);
            washServiceRepository.flush(); // Hemen veritabanına yansıt ki hata varsa burada yakalayalım
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new com.carwash.api.exception.BusinessException("Bu hizmet geçmiş veya aktif randevularda kullanıldığı için silinemez. Listeden kaldırmak yerine 'Pasif' duruma getirebilirsiniz.");
        }
    }

    private WashService findById(Long id) {
        return washServiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hizmet bulunamadı: " + id));
    }
}

