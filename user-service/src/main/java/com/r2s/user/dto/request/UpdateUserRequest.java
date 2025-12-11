package com.r2s.user.dto.request;

import lombok.Builder;

@Builder
public record UpdateUserRequest (
        String email,
        String name
) {}
