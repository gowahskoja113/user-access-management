package com.r2s.auth.dto.request;

import com.r2s.core.entity.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record RegisterRequest(
        @NotBlank(message = "Username cannot be blank")
        String username,

        @NotBlank(message = "Password cannot be blank")
        String password,

        @Email (message = "Invalid email format")
        String email,

        String name,

        RoleName roleName
) {
}
