package com.user.api.dto;

import com.user.api.enums.RoleName;

public record UpdateProfileDto(
        String name,
        RoleName role
) {
}