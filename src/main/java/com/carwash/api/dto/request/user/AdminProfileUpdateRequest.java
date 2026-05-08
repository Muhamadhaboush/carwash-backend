package com.carwash.api.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminProfileUpdateRequest {
    @NotBlank
    @Email
    private String email;
    
    private String password; // Optional: only update if provided
}
