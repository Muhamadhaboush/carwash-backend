package com.carwash.api.service;

import com.carwash.api.dto.request.auth.CorporateRegisterRequest;
import com.carwash.api.dto.request.auth.IndividualRegisterRequest;
import com.carwash.api.dto.request.auth.LoginRequest;
import com.carwash.api.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse registerIndividual(IndividualRegisterRequest request);
    AuthResponse registerCorporate(CorporateRegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);
}
