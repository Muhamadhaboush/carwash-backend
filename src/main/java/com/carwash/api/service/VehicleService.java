package com.carwash.api.service;

import com.carwash.api.dto.request.vehicle.VehicleRequest;
import com.carwash.api.dto.response.VehicleResponse;

import java.util.List;

public interface VehicleService {
    List<VehicleResponse> getMyVehicles(String email);
    VehicleResponse getVehicleById(String email, Long vehicleId);
    VehicleResponse addVehicle(String email, VehicleRequest request);
    VehicleResponse updateVehicle(String email, Long vehicleId, VehicleRequest request);
    void deleteVehicle(String email, Long vehicleId);
    VehicleResponse setDefault(String email, Long vehicleId);
}
