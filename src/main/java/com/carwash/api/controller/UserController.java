package com.carwash.api.controller;

import com.carwash.api.dto.request.user.ChangePasswordRequest;
import com.carwash.api.dto.request.user.UpdateProfileRequest;
import com.carwash.api.dto.response.UserResponse;
import com.carwash.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Kullanıcı", description = "Profil yönetimi ve admin kullanıcı işlemleri")
public class UserController {

    private final UserService userService;

    // ── Müşteri endpoint'leri ──────────────────────────────────────

    @Operation(summary = "Kendi profilimi getir")
    @GetMapping("/users/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getMe(userDetails.getUsername()));
    }

    @Operation(summary = "Profilimi güncelle (ad, soyad, telefon)")
    @PutMapping("/users/me")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(userDetails.getUsername(), request));
    }

    @Operation(summary = "Şifremi değiştir")
    @PutMapping("/users/me/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.noContent().build();
    }

    // ── Admin endpoint'leri ────────────────────────────────────────

    @Operation(summary = "[ADMIN] Tüm kullanıcıları listele (sayfalı)")
    @GetMapping("/admin/users")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsersPaged(pageable));
    }

    @Operation(summary = "[ADMIN] Kullanıcı detayını getir")
    @GetMapping("/admin/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(summary = "[ADMIN] Kullanıcıyı aktif/pasif yap")
    @PatchMapping("/admin/users/{id}/status")
    public ResponseEntity<UserResponse> toggleUserStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.toggleUserStatus(id, userDetails.getUsername()));
    }

    @Operation(summary = "[ADMIN] Kendi email/şifresini güncelle")
    @PutMapping("/admin/profile")
    public ResponseEntity<UserResponse> updateAdminProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody com.carwash.api.dto.request.user.AdminProfileUpdateRequest request) {
        return ResponseEntity.ok(userService.updateAdminProfile(userDetails.getUsername(), request));
    }
}
