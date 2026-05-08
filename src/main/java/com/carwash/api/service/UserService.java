package com.carwash.api.service;

import com.carwash.api.dto.request.user.ChangePasswordRequest;
import com.carwash.api.dto.request.user.UpdateProfileRequest;
import com.carwash.api.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    UserResponse getMe(String email);
    UserResponse updateProfile(String email, UpdateProfileRequest request);
    void changePassword(String email, ChangePasswordRequest request);

    List<UserResponse> getAllUsers();
    Page<UserResponse> getAllUsersPaged(Pageable pageable);
    UserResponse getUserById(Long id);
    UserResponse toggleUserStatus(Long id, String adminEmail);
    UserResponse updateAdminProfile(String currentEmail, com.carwash.api.dto.request.user.AdminProfileUpdateRequest request);
}
