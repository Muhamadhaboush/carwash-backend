package com.carwash.api.controller;

import com.carwash.api.dto.request.auth.CorporateRegisterRequest;
import com.carwash.api.dto.request.auth.IndividualRegisterRequest;
import com.carwash.api.dto.request.auth.LoginRequest;
import com.carwash.api.dto.response.AuthResponse;
import com.carwash.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/individual")
    public ResponseEntity<AuthResponse> registerIndividual(@Valid @RequestBody IndividualRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerIndividual(request));
    }

    @PostMapping("/register/corporate")
    public ResponseEntity<AuthResponse> registerCorporate(@Valid @RequestBody CorporateRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerCorporate(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String authHeader) {
        String refreshToken = authHeader.substring(7);
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> body) {
        authService.forgotPassword(body.get("email"));
        // Her zaman 200 dön (güvenlik: email varlığını açıklama)
        return ResponseEntity.ok(Map.of("message", "Eğer bu e-posta sistemde kayıtlıysa, sıfırlama bağlantısı gönderildi."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> body) {
        authService.resetPassword(body.get("token"), body.get("newPassword"));
        return ResponseEntity.ok(Map.of("message", "Şifreniz başarıyla güncellendi."));
    }
}
