package com.carwash.api.service;

import com.carwash.api.dto.request.address.AddressRequest;
import com.carwash.api.dto.response.AddressResponse;

import java.util.List;

public interface AddressService {
    List<AddressResponse> getMyAddresses(String email);
    AddressResponse addAddress(String email, AddressRequest request);
    AddressResponse updateAddress(String email, Long addressId, AddressRequest request);
    void deleteAddress(String email, Long addressId);
    AddressResponse setDefault(String email, Long addressId);
}
