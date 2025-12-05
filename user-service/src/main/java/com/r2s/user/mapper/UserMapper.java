package com.r2s.user.mapper;

import com.r2s.user.dto.response.UserResponse;
import com.r2s.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .username(user.getUsername())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }
}