package com.carwash.api.service;

import com.carwash.api.dto.request.service.WashServiceRequest;
import com.carwash.api.dto.response.WashServiceResponse;

import java.util.List;

public interface WashServiceService {
    List<WashServiceResponse> getActiveServices();
    List<WashServiceResponse> getAllServices();
    WashServiceResponse getServiceById(Long id);
    WashServiceResponse createService(WashServiceRequest request);
    WashServiceResponse updateService(Long id, WashServiceRequest request);
    WashServiceResponse toggleStatus(Long id);
    void deleteService(Long id);
}
