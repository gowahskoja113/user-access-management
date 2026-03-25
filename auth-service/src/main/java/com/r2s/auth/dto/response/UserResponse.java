package com.r2s.auth.dto.response;


import com.r2s.auth.entity.Role;

import java.util.Set;

public record UserResponse(
        Set<Role> roles,
        String email,
        String name,
        String username
) {}
