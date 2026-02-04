package com.r2s.user.service.impl;

import com.r2s.core.entity.User;
import com.r2s.core.exception.CustomException;
import com.r2s.core.repository.UserRepository;
import com.r2s.user.dto.request.UpdateUserRequest;
import com.r2s.user.dto.request.UserRequest;
import com.r2s.core.response.UserResponse;
import com.r2s.user.mapper.UserMapper;
import com.r2s.user.service.UserManagementService;
import com.r2s.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserManagementService, UserProfileService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(UserRequest request) {
        log.debug("Creating new user with username: {}", request.username());

        if (userRepository.existsByUsername(request.username())) {
            throw new CustomException("Username already exists: " + request.username());
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new CustomException("Email already exists: " + request.email());
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));

        try {
            User savedUser = userRepository.save(user);
            return userMapper.toUserResponse(savedUser);
        } catch (DataIntegrityViolationException e) {
            log.error("Database constraint violation: {}", e.getMessage());
            throw new CustomException("User creation failed. Username or Email likely already exists.");
        }
    }

    @Override
    public void deleteUser(String username) {
        log.debug("Deleting user with username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found with username: " + username));
        userRepository.delete(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users from the database");

        return userRepository.findAll()
                .stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponse updateUser(String username, UpdateUserRequest request) {
        log.debug("Updating user with username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found with username: " + username));

        user.setName(request.name());
        user.setEmail(request.email());

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    public UserResponse getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toUserResponse)
                .orElseThrow(() -> new CustomException("User not found with username: " + username));
    }
}
