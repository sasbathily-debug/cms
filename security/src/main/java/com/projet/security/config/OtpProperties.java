package com.projet.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.otp")
public record OtpProperties(long ttlMinutes) {
}
