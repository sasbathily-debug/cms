package com.projet.security.service;

import com.projet.security.config.OtpProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final String KEY_PREFIX = "otp:";
    private final SecureRandom random = new SecureRandom();

    private final RedisTemplate<String, String> redisTemplate;
    private final OtpProperties otpProperties;

    public String generate(String email) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        redisTemplate.opsForValue().set(KEY_PREFIX + email, code, Duration.ofMinutes(otpProperties.ttlMinutes()));
        return code;
    }

    public boolean verify(String email, String code) {
        String stored = redisTemplate.opsForValue().get(KEY_PREFIX + email);
        boolean valid = stored != null && stored.equals(code);
        if (valid) {
            redisTemplate.delete(KEY_PREFIX + email);
        }
        return valid;
    }
}
