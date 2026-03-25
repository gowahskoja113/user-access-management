package com.r2s.user.service.impl;

import com.r2s.user.dto.response.UserResponse;
import com.r2s.user.entity.UserProfile;
import com.r2s.core.exception.CustomException;
import com.r2s.user.mapper.OutboxMapper;
import com.r2s.user.repository.OutboxRepository;
import com.r2s.user.repository.UserProfileRepository;
import com.r2s.user.dto.request.UpdateUserRequest;
import com.r2s.user.mapper.UserMapper;
import com.r2s.user.service.UserManagementService;
import com.r2s.user.service.UserProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserManagementService, UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserMapper userMapper;
    private final OutboxMapper outboxMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional // Phải có Transactional để xóa Profile và ghi Outbox cùng lúc
    public void deleteUser(String username) {
        log.debug("Deleting user with username: {}", username);

        UserProfile userProfile = userProfileRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found with username: " + username));

        // 1. Lưu vào Outbox trước khi xóa hoặc dùng ID của nó
        outboxMapper.saveToOutbox("USER", "USER_DELETED", String.format("{\"id\":\"%s\"}", userProfile.getId()));

        // 2. Xóa ở local DB
        userProfileRepository.delete(userProfile);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users from the database");

        return userProfileRepository.findAll()
                .stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponse updateUser(String username, UpdateUserRequest request) {
        log.debug("Updating user with username: {}", username);

        UserProfile user = userProfileRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found with username: " + username));

        user.setFullName(request.name());
        user.setEmail(request.email());
        UserProfile updatedUser = userProfileRepository.save(user);

        try {
            String payload = objectMapper.writeValueAsString(java.util.Map.of(
                    "id", updatedUser.getId(),
                    "email", updatedUser.getEmail(),
                    "fullName", updatedUser.getFullName()
            ));
            outboxMapper.saveToOutbox("USER", "USER_UPDATED", payload);
        } catch (Exception e) {
            log.error("Lỗi parse JSON Outbox: {}", e.getMessage());
        }

        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    public UserResponse getUserByUsername(String username) {
        return userProfileRepository.findByUsername(username)
                .map(userMapper::toUserResponse)
                .orElseThrow(() -> new CustomException("User not found with username: " + username));
    }

}
