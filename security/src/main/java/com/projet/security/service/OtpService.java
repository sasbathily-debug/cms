package com.projet.security.service;

import com.projet.security.config.OtpProperties;
import com.projet.security.exception.OtpRateLimitException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class OtpService {

    /** Codes de l'inscription : clés historiques, inchangées. */
    private static final String REGISTRATION = "";
    /** Codes de la réinitialisation du mot de passe : clés séparées, un code d'inscription n'ouvre pas une réinitialisation. */
    private static final String RESET = "reset:";
    /** Codes de la prise de rendez-vous sans compte : un code de rendez-vous n'ouvre ni une inscription ni une réinitialisation. */
    private static final String BOOKING = "booking:";
    /** Au-delà, le code est détruit : 6 chiffres ne doivent pas pouvoir être devinés par essais successifs. */
    private static final int MAX_ATTEMPTS = 5;

    private final SecureRandom random = new SecureRandom();

    private final RedisTemplate<String, String> redisTemplate;
    private final OtpProperties otpProperties;

    /**
     * A la fois generation du code ET pose du cooldown, en une seule operation
     * atomique : evite qu'un appel concurrent passe entre les deux.
     */
    public String generate(String email) {
        return generate(REGISTRATION, email);
    }

    public boolean verify(String email, String code) {
        return verify(REGISTRATION, email, code);
    }

    public String generateForPasswordReset(String email) {
        return generate(RESET, email);
    }

    public boolean verifyForPasswordReset(String email, String code) {
        return verify(RESET, email, code);
    }

    public String generateForBooking(String email) {
        return generate(BOOKING, email);
    }

    public boolean verifyForBooking(String email, String code) {
        return verify(BOOKING, email, code);
    }

    private String generate(String purpose, String email) {
        Duration cooldown = Duration.ofSeconds(otpProperties.resendCooldownSeconds());
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(cooldownKey(purpose, email), "1", cooldown);
        if (Boolean.FALSE.equals(acquired)) {
            throw new OtpRateLimitException("Veuillez patienter avant de redemander un code.");
        }

        String code = String.format("%06d", random.nextInt(1_000_000));
        redisTemplate.opsForValue().set(codeKey(purpose, email), code, Duration.ofMinutes(otpProperties.ttlMinutes()));
        redisTemplate.delete(attemptsKey(purpose, email));
        return code;
    }

    private boolean verify(String purpose, String email, String code) {
        String stored = redisTemplate.opsForValue().get(codeKey(purpose, email));
        if (stored == null) {
            return false;
        }
        if (stored.equals(code)) {
            redisTemplate.delete(codeKey(purpose, email));
            redisTemplate.delete(attemptsKey(purpose, email));
            return true;
        }
        Long attempts = redisTemplate.opsForValue().increment(attemptsKey(purpose, email));
        redisTemplate.expire(attemptsKey(purpose, email), Duration.ofMinutes(otpProperties.ttlMinutes()));
        if (attempts != null && attempts >= MAX_ATTEMPTS) {
            redisTemplate.delete(codeKey(purpose, email));
            redisTemplate.delete(attemptsKey(purpose, email));
        }
        return false;
    }

    private static String codeKey(String purpose, String email) {
        return "otp:" + purpose + email;
    }

    private static String cooldownKey(String purpose, String email) {
        return "otp-cooldown:" + purpose + email;
    }

    private static String attemptsKey(String purpose, String email) {
        return "otp-attempts:" + purpose + email;
    }
}
