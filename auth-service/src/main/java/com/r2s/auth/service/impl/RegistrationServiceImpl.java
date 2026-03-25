package com.r2s.auth.service.impl;

import com.r2s.auth.dto.request.RegisterRequest;
import com.r2s.auth.service.RegistrationService;
import com.r2s.auth.entity.Outbox;
import com.r2s.auth.entity.Role;
import com.r2s.core.entity.RoleName;
import com.r2s.auth.entity.User;
import com.r2s.core.exception.CustomException;
import com.r2s.auth.repository.OutboxRepository;
import com.r2s.auth.repository.RoleRepository;
import com.r2s.auth.repository.UserRepository;
import com.r2s.auth.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final OutboxRepository outboxRepository;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new CustomException("Username already exists");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new CustomException("Email already exists");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .name(request.name())
                .password(passwordEncoder.encode(request.password()))
                .enabled(true)
                .roles(new HashSet<>())
                .build();

        RoleName targetRoleName = (request.roleName() != null) ? request.roleName() : RoleName.ROLE_USER;

        Role role = roleRepository.findByName(targetRoleName)
                .orElseThrow(() -> new CustomException("Error: Role is not found."));

        user.getRoles().add(role);

        User savedUser = userRepository.save(user);

        Set<RoleName> roleNames = savedUser.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        try {
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("id", savedUser.getId());
            payload.put("username", savedUser.getUsername());
            payload.put("email", savedUser.getEmail());
            payload.put("fullName", savedUser.getName());
            payload.put("roleName", targetRoleName);

            String jsonPayload = objectMapper.writeValueAsString(payload);

            Outbox outbox = Outbox.builder()
                    .aggregateType("USER")
                    .eventType("USER_CREATED")
                    .payload(jsonPayload)
                    .status("PENDING")
                    .createdAt(java.time.LocalDateTime.now())
                    .build();

            outboxRepository.save(outbox);

        } catch (Exception e) {
            throw new CustomException("Failed to sync user data: " + e.getMessage());
        }
        return new UserResponse(
                savedUser.getRoles(),
                savedUser.getEmail(),
                savedUser.getName(),
                savedUser.getUsername()
        );
    }
}
