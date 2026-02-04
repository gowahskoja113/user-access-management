package com.r2s.auth.service;

import com.r2s.auth.dto.request.RegisterRequest;
import com.r2s.core.response.UserResponse;

public interface RegistrationService {
    UserResponse register(RegisterRequest request);
}
