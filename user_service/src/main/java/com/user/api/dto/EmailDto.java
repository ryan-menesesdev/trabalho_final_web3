package com.user.api.dto;

import com.user.api.enums.RoleName;

import java.util.UUID;

public record EmailDto(
        String emailTo,
        String subject,
        String text,
        UUID userId
) {
}
