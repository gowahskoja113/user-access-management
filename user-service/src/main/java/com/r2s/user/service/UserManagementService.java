package com.r2s.user.service;

import com.r2s.user.dto.request.response.UserResponse;

import java.util.List;

public interface UserManagementService {
    void deleteUser(String username);
    public List<UserResponse> getAllUsers();
}
