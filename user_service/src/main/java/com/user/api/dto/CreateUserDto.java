package com.user.api.dto;

import com.user.api.enums.RoleName;

public record CreateUserDto(
        String email,
        String password,
        RoleName role
) {
}
