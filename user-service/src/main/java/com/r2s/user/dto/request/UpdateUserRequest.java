package com.r2s.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UpdateUserRequest (
        @Email(message = "Invalid email format")
        String email,
        @NotBlank(message = "Name cannot be blank")
        String name
) {}
