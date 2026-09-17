package com.projet.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String KEY_PREFIX = "blacklist:";

    private final RedisTemplate<String, String> redisTemplate;

    public void blacklist(String jti, Duration ttl) {
        if (!ttl.isNegative() && !ttl.isZero()) {
            redisTemplate.opsForValue().set(KEY_PREFIX + jti, "1", ttl);
        }
    }

    public boolean isBlacklisted(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + jti));
    }
}
