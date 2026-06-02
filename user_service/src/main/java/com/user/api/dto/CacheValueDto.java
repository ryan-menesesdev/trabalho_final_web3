package com.user.api.dto;

public record CacheValueDto(
        String code,
        long expiresAt
) {
}
