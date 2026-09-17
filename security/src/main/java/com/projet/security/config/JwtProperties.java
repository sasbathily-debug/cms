package com.projet.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        String secret,
        String issuer,
        long accessTokenTtlMinutes,
        long refreshTokenTtlDays
) {
}
