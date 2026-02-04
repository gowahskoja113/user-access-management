package com.r2s.auth.strategy;

import com.r2s.auth.dto.request.LoginRequest;
import com.r2s.auth.dto.response.AuthResponse;
import org.springframework.security.core.userdetails.UserDetails;

public interface AuthenticationStrategy {
    AuthResponse authenticate(LoginRequest request);
    UserDetails loadUser(String username);
    boolean supports(String authenticationType);
}
