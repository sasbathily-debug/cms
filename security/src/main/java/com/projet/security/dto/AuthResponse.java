package com.projet.security.dto;

public record AuthResponse(String accessToken, String refreshToken, long expiresAt) {
}
