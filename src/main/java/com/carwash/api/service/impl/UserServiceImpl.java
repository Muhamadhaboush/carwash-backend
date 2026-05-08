package com.carwash.api.service.impl;

import com.carwash.api.dto.request.user.ChangePasswordRequest;
import com.carwash.api.dto.request.user.UpdateProfileRequest;
import com.carwash.api.dto.response.UserResponse;
import com.carwash.api.entity.User;
import com.carwash.api.exception.BusinessException;
import com.carwash.api.exception.ResourceNotFoundException;
import com.carwash.api.repository.UserRepository;
import com.carwash.api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getMe(String email) {
        return UserResponse.from(getUser(email));
    }

    @Override
    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = getUser(email);
        log.info("Profil güncelleniyor: {}", email);

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getCompanyName() != null) user.setCompanyName(request.getCompanyName());
        if (request.getTaxNumber() != null) user.setTaxNumber(request.getTaxNumber());

        return UserResponse.from(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = getUser(email);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Mevcut şifre hatalı.");
        }
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new BusinessException("Yeni şifre ve tekrarı eşleşmiyor.");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException("Yeni şifre mevcut şifre ile aynı olamaz.");
        }

        log.info("Şifre değiştiriliyor: {}", email);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream().map(UserResponse::from).collect(Collectors.toList());
    }

    @Override
    public Page<UserResponse> getAllUsersPaged(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponse::from);
    }

    @Override
    public UserResponse getUserById(Long id) {
        return UserResponse.from(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + id)));
    }

    @Override
    @Transactional
    public UserResponse toggleUserStatus(Long id, String adminEmail) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + id));
        if (user.getEmail().equals(adminEmail)) {
            throw new BusinessException("Kendi hesabınızı pasif yapamazsınız.");
        }
        user.setActive(!user.isActive());
        log.info("Kullanıcı durumu değiştirildi - ID: {}, yeni durum: active={}", id, user.isActive());
        return UserResponse.from(userRepository.save(user));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı."));
    }

    @Override
    @Transactional
    public UserResponse updateAdminProfile(String currentEmail, com.carwash.api.dto.request.user.AdminProfileUpdateRequest request) {
        User admin = getUser(currentEmail);
        
        // Eğer email değişiyorsa, yeni email'in başka biri tarafından kullanılmadığından emin ol
        if (!admin.getEmail().equalsIgnoreCase(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("Bu e-posta adresi zaten başka bir kullanıcı tarafından kullanılıyor.");
            }
            admin.setEmail(request.getEmail());
            log.info("Admin e-postası değiştirildi: {} -> {}", currentEmail, request.getEmail());
        }
        
        // Şifre girilmişse güncelle
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            admin.setPassword(passwordEncoder.encode(request.getPassword()));
            log.info("Admin şifresi güncellendi.");
        }
        
        return UserResponse.from(userRepository.save(admin));
    }
}
