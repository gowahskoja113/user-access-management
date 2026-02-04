package com.r2s.auth.service.impl;

import com.r2s.auth.dto.request.RegisterRequest;
import com.r2s.auth.service.RegistrationService;
import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;
import com.r2s.core.exception.CustomException;
import com.r2s.core.repository.UserRepository;
import com.r2s.core.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new CustomException("Username already exists");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new CustomException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setName(request.name());
        user.setPassword(passwordEncoder.encode(request.password()));
        if (request.role() != null) {
            user.setRole(request.role());
        } else {
            user.setRole(Role.ROLE_USER);
        }

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getRole(),
                savedUser.getEmail(),
                savedUser.getName(),
                savedUser.getUsername()
        );
    }
}
