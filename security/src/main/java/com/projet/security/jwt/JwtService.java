package com.projet.security.jwt;

import com.projet.security.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    public GeneratedToken generateAccessToken(UUID userId, String email, String role) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtProperties.accessTokenTtlMinutes(), ChronoUnit.MINUTES);
        String jti = UUID.randomUUID().toString();
        String token = Jwts.builder()
                .subject(userId.toString())
                .id(jti)
                .issuer(jwtProperties.issuer())
                .claim("email", email)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey())
                .compact();
        return new GeneratedToken(token, jti, expiry);
    }

    public GeneratedToken generateRefreshToken(UUID userId) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtProperties.refreshTokenTtlDays(), ChronoUnit.DAYS);
        String jti = UUID.randomUUID().toString();
        String token = Jwts.builder()
                .subject(userId.toString())
                .id(jti)
                .issuer(jwtProperties.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey())
                .compact();
        return new GeneratedToken(token, jti, expiry);
    }

    public Claims parseAndValidate(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes());
    }

    public record GeneratedToken(String token, String jti, Instant expiresAt) {
    }
}
