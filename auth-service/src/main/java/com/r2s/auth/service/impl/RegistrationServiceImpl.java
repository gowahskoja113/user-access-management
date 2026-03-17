package com.r2s.auth.service.impl;

import com.r2s.auth.dto.request.RegisterRequest;
import com.r2s.auth.service.RegistrationService;
import com.r2s.core.entity.Role;
import com.r2s.core.entity.RoleName;
import com.r2s.core.entity.User;
import com.r2s.core.exception.CustomException;
import com.r2s.core.repository.RoleRepository;
import com.r2s.core.repository.UserRepository;
import com.r2s.core.response.UserResponse;
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

        return new UserResponse(
                savedUser.getRoles(),
                savedUser.getEmail(),
                savedUser.getName(),
                savedUser.getUsername()
        );
    }
}
