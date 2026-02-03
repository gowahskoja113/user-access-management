package com.r2s.user.service;

import com.r2s.user.dto.request.UserRequest;
import com.r2s.user.dto.response.UserResponse;

import java.util.List;

public interface UserManagementService {
    UserResponse createUser(UserRequest request);
    void deleteUser(String username);
    public List<UserResponse> getAllUsers();
}
