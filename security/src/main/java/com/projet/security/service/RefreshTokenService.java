package com.projet.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String KEY_PREFIX = "refresh:";

    private final RedisTemplate<String, String> redisTemplate;

    public void store(UUID userId, String jti, Duration ttl) {
        redisTemplate.opsForValue().set(KEY_PREFIX + userId, jti, ttl);
    }

    public boolean isValid(UUID userId, String jti) {
        String stored = redisTemplate.opsForValue().get(KEY_PREFIX + userId);
        return jti.equals(stored);
    }

    public void revoke(UUID userId) {
        redisTemplate.delete(KEY_PREFIX + userId);
    }
}
