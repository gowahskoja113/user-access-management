package com.r2s.auth.strategy;

import com.r2s.auth.dto.request.LoginRequest;
import com.r2s.auth.dto.response.AuthResponse;
import com.r2s.core.entity.User;
import com.r2s.core.repository.UserRepository;
import com.r2s.core.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocalAuthenticationStrategy implements AuthenticationStrategy {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public boolean supports(String authenticationType) {
        return "LOCAL".equalsIgnoreCase(authenticationType);
    }

    @Override
    public AuthResponse authenticate(LoginRequest request) {

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found")
                );

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthResponse(token);
    }

    @Override
    public UserDetails loadUser(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found")
                );

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRole().name())
                .build();
    }
}
