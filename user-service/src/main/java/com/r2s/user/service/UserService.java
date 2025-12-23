package com.r2s.user.service;

import com.r2s.core.entity.User;
import com.r2s.core.repository.UserRepository;
import com.r2s.user.dto.request.UpdateUserRequest;
import com.r2s.user.dto.request.UserRequest;
import com.r2s.user.dto.response.UserResponse;
import com.r2s.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toUserResponse)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserResponse updateUser(String username, UpdateUserRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(request.name());
        user.setEmail(request.email());

        User savedUser = userRepository.save(user);

        return userMapper.toUserResponse(savedUser);
    }

    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }

    public User createUser(UserRequest request) {
        // Đoạn check cũ này cứ giữ nguyên (hoặc bỏ cũng được)
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }

        User newUser = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .name(request.fullName())
                .email(request.email())
                .role(request.role())
                .build();

        // --- THÊM ĐOẠN TRY-CATCH NÀY VÀO ---
        try {
            return userRepository.save(newUser);
        } catch (DataIntegrityViolationException e) {
            // Nếu DB báo lỗi trùng (do unique constraint), ta ném ra 400
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username or Email already exists");
        }
    }
}
