package com.r2s.auth.service.impl;

import com.r2s.auth.dto.response.AuthResponse;
import com.r2s.auth.dto.request.LoginRequest;

import com.r2s.auth.service.AuthenticationService;
import com.r2s.auth.strategy.AuthenticationStrategy;
import com.r2s.core.exception.CustomException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
@AllArgsConstructor
@Slf4j
@Service
public class AuthServiceImpl implements AuthenticationService {

    private final List<AuthenticationStrategy> strategies;

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
}
