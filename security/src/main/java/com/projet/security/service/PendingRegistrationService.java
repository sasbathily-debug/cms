package com.projet.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * Holds the name/phone collected at step 1 of registration in Redis until the
 * email is verified (step 2) and a password is chosen (step 3) — no User row
 * is created until then, so an abandoned registration leaves nothing behind.
 */
@Service
@RequiredArgsConstructor
public class PendingRegistrationService {

    private static final String DATA_PREFIX = "pending-registration:";
    private static final String VERIFIED_PREFIX = "pending-registration-verified:";
    private static final Duration TTL = Duration.ofMinutes(30);

    private final RedisTemplate<String, String> redisTemplate;

    public void save(String email, String firstName, String lastName, String phone) {
        String key = DATA_PREFIX + email;
        redisTemplate.<String, String>opsForHash().putAll(key, Map.of(
                "firstName", firstName,
                "lastName", lastName,
                "phone", phone
        ));
        redisTemplate.expire(key, TTL);
    }

    public Optional<PendingRegistration> find(String email) {
        Map<String, String> data = redisTemplate.<String, String>opsForHash().entries(DATA_PREFIX + email);
        if (data.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new PendingRegistration(data.get("firstName"), data.get("lastName"), data.get("phone")));
    }

    public void markVerified(String email) {
        redisTemplate.opsForValue().set(VERIFIED_PREFIX + email, "true", TTL);
    }

    public boolean isVerified(String email) {
        return Boolean.parseBoolean(redisTemplate.opsForValue().get(VERIFIED_PREFIX + email));
    }

    public void clear(String email) {
        redisTemplate.delete(DATA_PREFIX + email);
        redisTemplate.delete(VERIFIED_PREFIX + email);
    }
}
