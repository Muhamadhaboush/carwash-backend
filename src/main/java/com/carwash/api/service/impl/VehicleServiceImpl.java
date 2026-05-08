package com.carwash.api.service.impl;

import com.carwash.api.dto.request.vehicle.VehicleRequest;
import com.carwash.api.dto.response.VehicleResponse;
import com.carwash.api.entity.User;
import com.carwash.api.entity.Vehicle;
import com.carwash.api.exception.BusinessException;
import com.carwash.api.exception.ResourceNotFoundException;
import com.carwash.api.exception.UnauthorizedException;
import com.carwash.api.repository.UserRepository;
import com.carwash.api.repository.VehicleRepository;
import com.carwash.api.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getMyVehicles(String email) {
        User user = getUser(email);
        return vehicleRepository.findAllByUserId(user.getId())
                .stream().map(VehicleResponse::from).collect(Collectors.toList());
    }

    @Override
    public VehicleResponse getVehicleById(String email, Long vehicleId) {
        User user = getUser(email);
        Vehicle vehicle = vehicleRepository.findByIdAndUserId(vehicleId, user.getId())
                .orElseThrow(() -> new UnauthorizedException("Bu araca erişim yetkiniz yok."));
        return VehicleResponse.from(vehicle);
    }

    @Override
    @Transactional
    public VehicleResponse addVehicle(String email, VehicleRequest request) {
        User user = getUser(email);
        if (vehicleRepository.existsByUserIdAndPlateNumber(user.getId(), request.getPlateNumber())) {
            throw new BusinessException("Bu plaka zaten kayıtlı: " + request.getPlateNumber());
        }
        Vehicle vehicle = Vehicle.builder()
                .user(user)
                .plateNumber(request.getPlateNumber().toUpperCase())
                .brand(request.getBrand())
                .model(request.getModel())
                .vehicleType(request.getVehicleType())
                .isDefault(false)
                .build();
        return VehicleResponse.from(vehicleRepository.save(vehicle));
    }

    @Override
    @Transactional
    public VehicleResponse updateVehicle(String email, Long vehicleId, VehicleRequest request) {
        User user = getUser(email);
        Vehicle vehicle = vehicleRepository.findByIdAndUserId(vehicleId, user.getId())
                .orElseThrow(() -> new UnauthorizedException("Bu araca erişim yetkiniz yok."));
        if (!vehicle.getPlateNumber().equalsIgnoreCase(request.getPlateNumber()) &&
                vehicleRepository.existsByUserIdAndPlateNumber(user.getId(), request.getPlateNumber())) {
            throw new BusinessException("Bu plaka zaten kayıtlı: " + request.getPlateNumber());
        }
        vehicle.setPlateNumber(request.getPlateNumber().toUpperCase());
        vehicle.setBrand(request.getBrand());
        vehicle.setModel(request.getModel());
        vehicle.setVehicleType(request.getVehicleType());
        return VehicleResponse.from(vehicleRepository.save(vehicle));
    }

    @Override
    @Transactional
    public void deleteVehicle(String email, Long vehicleId) {
        User user = getUser(email);
        Vehicle vehicle = vehicleRepository.findByIdAndUserId(vehicleId, user.getId())
                .orElseThrow(() -> new UnauthorizedException("Bu araca erişim yetkiniz yok."));
        if (!vehicle.getAppointments().isEmpty()) {
            throw new BusinessException("Bu araca ait randevu kayıtları olduğu için silinemez.");
        }
        vehicleRepository.delete(vehicle);
    }

    @Override
    @Transactional
    public VehicleResponse setDefault(String email, Long vehicleId) {
        User user = getUser(email);
        Vehicle target = vehicleRepository.findByIdAndUserId(vehicleId, user.getId())
                .orElseThrow(() -> new UnauthorizedException("Bu araca erişim yetkiniz yok."));
        vehicleRepository.findByUserIdAndIsDefaultTrue(user.getId()).ifPresent(current -> {
            current.setDefault(false);
            vehicleRepository.save(current);
        });
        target.setDefault(true);
        return VehicleResponse.from(vehicleRepository.save(target));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı."));
    }
}

