package com.r2s.auth.service;

import com.r2s.auth.dto.request.LoginRequest;
import com.r2s.auth.dto.response.AuthResponse;

public interface AuthenticationService {
    public AuthResponse login(LoginRequest request, String authType);
}
