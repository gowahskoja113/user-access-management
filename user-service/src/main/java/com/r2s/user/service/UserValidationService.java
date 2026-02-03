package com.r2s.user.service;

import com.r2s.user.dto.request.UpdateUserRequest;
import com.r2s.user.dto.request.UserRequest;

public interface UserValidationService {
    void validateUserCreation(UserRequest request);
    void validateUserUpdate(String username, UpdateUserRequest request);
}
