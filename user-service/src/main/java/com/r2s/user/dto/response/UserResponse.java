package com.r2s.user.dto.response;

public record UserResponse(
        String role,
        String email,
        String name,
        String username
) {

}
