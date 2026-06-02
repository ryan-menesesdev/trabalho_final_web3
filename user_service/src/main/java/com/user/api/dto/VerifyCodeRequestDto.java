package com.user.api.dto;

public record VerifyCodeRequestDto(
        String email,
        String code
) {
}
