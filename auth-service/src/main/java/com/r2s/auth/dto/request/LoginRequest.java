package com.r2s.auth.dto.request;

public record LoginRequest (
        String username,
        String password
) {}
