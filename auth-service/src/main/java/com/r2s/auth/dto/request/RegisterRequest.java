package com.r2s.auth.dto.request;

import com.r2s.auth.entity.Role;

public record RegisterRequest (
        String username,
        String password,
        String email,
        String name,
        Role role
){}
