package com.r2s.user.mapper;

import com.r2s.core.entity.User;
import com.r2s.user.dto.request.UserRequest;
import com.r2s.user.dto.response.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }

        return new UserResponse(
                user.getRole(),
                user.getEmail(),
                user.getName(),
                user.getUsername()
        );
    }

    // username, password, fullName, email, role
    public User toEntity(UserRequest request) {
        if (request == null) {
            return null;
        }

        User user = new User();
        user.setUsername(request.username());
        user.setName(request.fullName());
        user.setEmail(request.email());
        user.setRole(request.role());

        return user;
    }
}