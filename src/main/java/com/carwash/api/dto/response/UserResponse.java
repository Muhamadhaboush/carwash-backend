package com.carwash.api.dto.response;

import com.carwash.api.entity.User;
import com.carwash.api.enums.Role;
import com.carwash.api.enums.UserType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Role role;
    private UserType userType;
    private String companyName;
    private String taxNumber;
    private boolean isActive;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole())
                .userType(user.getUserType())
                .companyName(user.getCompanyName())
                .taxNumber(user.getTaxNumber())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
