package com.user.api.services;

import com.user.api.dto.CacheValueDto;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CodigoCacheService {
    private static final long EXPIRATION_MILLIS = 5 * 60 * 1000L;
    private final ConcurrentHashMap<String, CacheValueDto> cache = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public String generateAndStore(String email) {
        String code = generate6DigitCode();
        long expiresAt = Instant.now().toEpochMilli() + EXPIRATION_MILLIS;

        cache.put(email, new CacheValueDto(code, expiresAt));

        return code;
    }

    public boolean validate(String email, String code) {
        CacheValueDto cv = cache.get(email);

        if (cv == null) return false;

        if (!cv.code().equals(code)) return false;

        if (cv.expiresAt() < Instant.now().toEpochMilli()) {
            cache.remove(email);
            return false;
        }

        return true;
    }

    public void remove(String email) {
        cache.remove(email);
    }

    @Scheduled(fixedRate = 60_000)
    public void cleanupExpired() {
        for (Map.Entry<String, CacheValueDto> e : cache.entrySet()) {
            if (e.getValue().expiresAt() < Instant.now().toEpochMilli()) {
                cache.remove(e.getKey());
            }
        }
    }

    private String generate6DigitCode() {
        return String.format("%06d", random.nextInt(1_000_000));
    }
}
