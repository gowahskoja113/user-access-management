package com.r2s.auth.dto.request;

import com.r2s.core.entity.Role;
import lombok.Builder;

@Builder
public record RegisterRequest(
        String username,
        String password,
        String email,
        String name,
        Role role
) {
}
