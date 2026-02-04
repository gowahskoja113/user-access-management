package com.r2s.user.service;

import com.r2s.user.dto.request.UpdateUserRequest;
import com.r2s.core.response.UserResponse;

public interface UserProfileService {
    UserResponse updateUser(String username, UpdateUserRequest request);
    UserResponse getUserByUsername(String username);
}
