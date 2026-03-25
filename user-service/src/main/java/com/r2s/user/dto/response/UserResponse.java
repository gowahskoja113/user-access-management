package com.r2s.user.dto.response;

public record UserResponse(
        String email,
        String name,
        String username
) {}
