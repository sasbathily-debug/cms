package com.projet.security.service;

import com.projet.security.config.CookieProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class AuthCookieService {

    public static final String ACCESS_COOKIE = "access_token";
    public static final String REFRESH_COOKIE = "refresh_token";

    private final CookieProperties cookieProperties;

    public void setAccessCookie(HttpServletResponse response, String token, Duration maxAge) {
        response.addHeader("Set-Cookie", build(ACCESS_COOKIE, token, maxAge).toString());
    }

    public void setRefreshCookie(HttpServletResponse response, String token, Duration maxAge) {
        response.addHeader("Set-Cookie", build(REFRESH_COOKIE, token, maxAge).toString());
    }

    public void clearCookies(HttpServletResponse response) {
        response.addHeader("Set-Cookie", build(ACCESS_COOKIE, "", Duration.ZERO).toString());
        response.addHeader("Set-Cookie", build(REFRESH_COOKIE, "", Duration.ZERO).toString());
    }

    public String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private ResponseCookie build(String name, String value, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .sameSite(cookieProperties.sameSite())
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}
