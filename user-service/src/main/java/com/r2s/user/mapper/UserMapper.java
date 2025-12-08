package com.r2s.user.mapper;

import com.r2s.core.entity.User;
import com.r2s.user.dto.response.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }

        return new UserResponse(
                user.getRole() != null ? user.getRole().name() : null,
                user.getEmail(),
                user.getName(),
                user.getUsername()
        );
    }
}