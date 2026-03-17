package com.r2s.core.response;

import com.r2s.core.entity.Role;

import java.util.Set;

public record UserResponse(
        Set<Role> roles,
        String email,
        String name,
        String username
) {}
