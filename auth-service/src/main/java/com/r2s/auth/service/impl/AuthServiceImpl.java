package com.r2s.auth.service.impl;

import com.r2s.auth.dto.request.RegisterRequest;
import com.r2s.auth.dto.response.AuthResponse;
import com.r2s.auth.dto.request.LoginRequest;

import com.r2s.auth.service.AuthenticationService;
import com.r2s.auth.service.RegistrationService;
import com.r2s.auth.strategy.AuthenticationStrategy;
import com.r2s.core.exception.CustomException;
import com.r2s.core.response.UserResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Slf4j
@Service
public class AuthServiceImpl implements AuthenticationService {

    private final List<AuthenticationStrategy> strategies;
    private final RegistrationService registrationService;

    @Override
    public AuthResponse login(LoginRequest request, String authType) {

        log.info("Attempting login for user: {}", request.username());

        return strategies.stream()
                .filter(strategy -> strategy.supports(authType))
                .findFirst()
                .orElseThrow(() ->
                        new CustomException("Unsupported auth type: " + authType)
                )
                .authenticate(request);
    }

    // Thêm hàm login mặc định để hỗ trợ các test case cũ gọi login(request)
    public AuthResponse login(LoginRequest request) {
        return login(request, "LOCAL");
    }

    // Thêm hàm register để hỗ trợ các test case gọi qua AuthServiceImpl
    public UserResponse register(RegisterRequest request) {
        return registrationService.register(request);
    }
}
