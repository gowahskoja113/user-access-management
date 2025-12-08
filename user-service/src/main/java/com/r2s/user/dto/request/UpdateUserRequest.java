package com.r2s.user.dto.request;


public record UpdateUserRequest (
        String email,
        String name
) {}
