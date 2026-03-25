package com.r2s.user.mapper;

import com.r2s.user.dto.request.UserRequest;
import com.r2s.user.dto.request.response.UserResponse;
import com.r2s.user.entity.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {


    public UserResponse toUserResponse(UserProfile user) {
        if (user == null) {
            return null;
        }

        return new UserResponse(
                user.getEmail(),
                user.getFullName(),
                user.getUsername()
        );
    }

    public UserProfile toEntity(UserRequest request) {
        if (request == null) {
            return null;
        }

        UserProfile user = new UserProfile();
        user.setUsername(request.username());
        user.setFullName(request.fullName());
        user.setEmail(request.email());

        return user;
    }
}