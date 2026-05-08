package com.carwash.api.service;

import com.carwash.api.dto.request.settings.BusinessSettingRequest;
import com.carwash.api.dto.response.BusinessSettingResponse;

import java.util.List;

public interface BusinessSettingService {
    List<BusinessSettingResponse> getAllSettings();
    BusinessSettingResponse getSettingByKey(String key);
    BusinessSettingResponse createSetting(BusinessSettingRequest request);
    BusinessSettingResponse updateSettingByKey(String key, BusinessSettingRequest request);
}
