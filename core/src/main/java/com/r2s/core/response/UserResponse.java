package com.r2s.core.response;

import com.r2s.core.entity.Role;

public record UserResponse(
        Role role,
        String email,
        String name,
        String username
) {}
