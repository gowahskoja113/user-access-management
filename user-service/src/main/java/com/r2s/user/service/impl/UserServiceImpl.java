package com.r2s.user.service.impl;

import com.r2s.user.dto.request.response.UserResponse;
import com.r2s.user.entity.UserProfile;
import com.r2s.core.exception.CustomException;
import com.r2s.user.repository.OutboxRepository;
import com.r2s.user.repository.UserProfileRepository;
import com.r2s.user.dto.request.UpdateUserRequest;
import com.r2s.user.mapper.UserMapper;
import com.r2s.user.service.UserManagementService;
import com.r2s.user.service.UserProfileService;
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
    private final PasswordEncoder passwordEncoder;
    private final OutboxRepository outboxRepository;

    @Override
    public void deleteUser(String username) {
        log.debug("Deleting user with username: {}", username);

        UserProfile userProfile = userProfileRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found with username: " + username));
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

        return userMapper.toUserResponse(userProfileRepository.save(user));
    }

    @Override
    public UserResponse getUserByUsername(String username) {
        return userProfileRepository.findByUsername(username)
                .map(userMapper::toUserResponse)
                .orElseThrow(() -> new CustomException("User not found with username: " + username));
    }
}
