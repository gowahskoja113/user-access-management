package com.r2s.user.dto.request;

import com.r2s.core.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record UserRequest(

        @NotBlank(message = "Username không được để trống")
        String username,

        @NotBlank(message = "Password không được để trống")
        String password,

        @NotBlank(message = "Full name không được để trống")
        String fullName,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        String email,

        @NotNull(message = "Role không được để trống")
        Role role
) {
}