package com.carwash.api.service.impl;

import com.carwash.api.dto.request.auth.CorporateRegisterRequest;
import com.carwash.api.dto.request.auth.IndividualRegisterRequest;
import com.carwash.api.dto.request.auth.LoginRequest;
import com.carwash.api.dto.response.AuthResponse;
import com.carwash.api.entity.Address;
import com.carwash.api.entity.PasswordResetToken;
import com.carwash.api.entity.User;
import com.carwash.api.enums.Role;
import com.carwash.api.enums.UserType;
import com.carwash.api.exception.BusinessException;
import com.carwash.api.repository.AddressRepository;
import com.carwash.api.repository.PasswordResetTokenRepository;
import com.carwash.api.repository.UserRepository;
import com.carwash.api.security.JwtService;
import com.carwash.api.service.AuthService;
import com.carwash.api.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public AuthResponse registerIndividual(IndividualRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Bu e-posta adresi zaten kayıtlı: " + request.getEmail());
        }
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(Role.CUSTOMER)
                .userType(UserType.INDIVIDUAL)
                .isActive(true)
                .build();
        User savedUser = userRepository.save(user);
        addressRepository.save(buildAddress(savedUser, request.getAddress()));
        return buildAuthResponse(savedUser);
    }

    @Override
    @Transactional
    public AuthResponse registerCorporate(CorporateRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Bu e-posta adresi zaten kayıtlı: " + request.getEmail());
        }
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .companyName(request.getCompanyName())
                .taxNumber(request.getTaxNumber())
                .role(Role.CUSTOMER)
                .userType(UserType.CORPORATE)
                .isActive(true)
                .build();
        User savedUser = userRepository.save(user);
        addressRepository.save(buildAddress(savedUser, request.getAddress()));
        return buildAuthResponse(savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Kullanıcı bulunamadı."));
        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        String email = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Kullanıcı bulunamadı."));
        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new BusinessException("Geçersiz veya süresi dolmuş refresh token.");
        }
        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .tokenType("Bearer")
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        // Kullanıcı yoksa sessizce devam et (güvenlik: email numaralandırmasını önle)
        userRepository.findByEmail(email).ifPresent(user -> {
            // Önceki tokenları sil
            passwordResetTokenRepository.deleteByUserId(user.getId());

            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .token(token)
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .used(false)
                    .build();
            passwordResetTokenRepository.save(resetToken);

            String resetLink = "http://localhost:3001/reset-password?token=" + token;
            emailService.sendPasswordResetEmail(user, resetLink);
            log.info("✅ Şifre sıfırlama maili gönderildi: {}", email);
        });
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("Şifre sıfırlama bağlantısı geçersiz."));

        if (resetToken.isUsed()) {
            throw new BusinessException("Bu bağlantı daha önce kullanılmış.");
        }
        if (resetToken.isExpired()) {
            throw new BusinessException("Şifre sıfırlama bağlantısının süresi dolmuş. Lütfen yeni bir istek oluşturun.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
        log.info("✅ Şifre sıfırlandı: {}", user.getEmail());
    }

    private Address buildAddress(User user, com.carwash.api.dto.request.address.AddressRequest req) {
        return Address.builder()
                .user(user)
                .label(req.getLabel())
                .street(req.getStreet())
                .district(req.getDistrict())
                .city(req.getCity())
                .postalCode(req.getPostalCode())
                .country(req.getCountry() != null ? req.getCountry() : "Türkiye")
                .isDefault(true)
                .build();
    }

    private AuthResponse buildAuthResponse(User user) {
        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .tokenType("Bearer")
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}

