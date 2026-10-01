package com.projet.security.jwt;

import com.projet.security.service.AuthCookieService;
import com.projet.security.service.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final AuthCookieService authCookieService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        response.setHeader("X-Debug-Filter-URI", request.getRequestURI());
        String token = authCookieService.readCookie(request, AuthCookieService.ACCESS_COOKIE);
        response.setHeader("X-Debug-Filter-TokenPresent", String.valueOf(token != null));
        if (token != null) {
            try {
                Claims claims = jwtService.parseAndValidate(token);
                response.setHeader("X-Debug-Filter-ParseOk", "true");
                response.setHeader("X-Debug-Filter-Jti", String.valueOf(claims.getId()));
                boolean blacklisted = tokenBlacklistService.isBlacklisted(claims.getId());
                response.setHeader("X-Debug-Filter-Blacklisted", String.valueOf(blacklisted));
                if (!blacklisted) {
                    String role = claims.get("role", String.class);
                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                    var authentication = new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    response.setHeader("X-Debug-Filter-SetAuth", "true");
                }
            } catch (JwtException e) {
                response.setHeader("X-Debug-Filter-ParseOk", "false");
                response.setHeader("X-Debug-Filter-ParseError", e.getClass().getSimpleName() + ":" + e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
