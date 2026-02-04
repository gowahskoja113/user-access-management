package com.r2s.auth.controller;

import com.r2s.auth.dto.request.LoginRequest;
import com.r2s.auth.dto.request.RegisterRequest;
import com.r2s.auth.dto.response.AuthResponse;
import com.r2s.auth.service.AuthenticationService;
import com.r2s.auth.service.RegistrationService;
import com.r2s.auth.service.impl.AuthServiceImpl;
import com.r2s.core.entity.User;
import com.r2s.core.response.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final RegistrationService registrationService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request, "LOCAL"));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(registrationService.register(request));
    }

}
