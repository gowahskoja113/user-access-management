package com.r2s.user.dto.request;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String email;
    private String name;
}
