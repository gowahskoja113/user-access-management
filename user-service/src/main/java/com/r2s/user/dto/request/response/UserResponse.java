package com.r2s.user.dto.request.response;

public record UserResponse(
        String email,
        String name,
        String username
) {}
