package com.carwash.api.service.impl;

import com.carwash.api.dto.request.address.AddressRequest;
import com.carwash.api.dto.response.AddressResponse;
import com.carwash.api.entity.Address;
import com.carwash.api.entity.User;
import com.carwash.api.exception.ResourceNotFoundException;
import com.carwash.api.exception.UnauthorizedException;
import com.carwash.api.repository.AddressRepository;
import com.carwash.api.repository.UserRepository;
import com.carwash.api.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    public List<AddressResponse> getMyAddresses(String email) {
        User user = getUser(email);
        return addressRepository.findAllByUserId(user.getId())
                .stream().map(AddressResponse::from).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AddressResponse addAddress(String email, AddressRequest request) {
        User user = getUser(email);
        Address address = Address.builder()
                .user(user)
                .label(request.getLabel())
                .street(request.getStreet())
                .district(request.getDistrict())
                .city(request.getCity())
                .postalCode(request.getPostalCode())
                .country(request.getCountry() != null ? request.getCountry() : "Türkiye")
                .isDefault(false)
                .build();
        return AddressResponse.from(addressRepository.save(address));
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(String email, Long addressId, AddressRequest request) {
        User user = getUser(email);
        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new UnauthorizedException("Bu adrese erişim yetkiniz yok."));
        address.setLabel(request.getLabel());
        address.setStreet(request.getStreet());
        address.setDistrict(request.getDistrict());
        address.setCity(request.getCity());
        address.setPostalCode(request.getPostalCode());
        if (request.getCountry() != null) address.setCountry(request.getCountry());
        return AddressResponse.from(addressRepository.save(address));
    }

    @Override
    @Transactional
    public void deleteAddress(String email, Long addressId) {
        User user = getUser(email);
        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new UnauthorizedException("Bu adrese erişim yetkiniz yok."));
        addressRepository.delete(address);
    }

    @Override
    @Transactional
    public AddressResponse setDefault(String email, Long addressId) {
        User user = getUser(email);
        Address target = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new UnauthorizedException("Bu adrese erişim yetkiniz yok."));
        addressRepository.findByUserIdAndIsDefaultTrue(user.getId()).ifPresent(current -> {
            current.setDefault(false);
            addressRepository.save(current);
        });
        target.setDefault(true);
        return AddressResponse.from(addressRepository.save(target));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı."));
    }
}

