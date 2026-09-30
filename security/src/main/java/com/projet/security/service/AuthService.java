package com.projet.security.service;

import com.projet.integration.notification.OtpChannel;
import com.projet.security.config.OtpProperties;
import com.projet.security.dto.CompleteRegistrationRequest;
import com.projet.security.dto.LoginRequest;
import com.projet.security.dto.RegisterRequest;
import com.projet.security.dto.RegisterResponse;
import com.projet.security.dto.VerifyOtpResponse;
import com.projet.security.exception.InvalidOtpException;
import com.projet.security.exception.InvalidTokenException;
import com.projet.security.jwt.JwtService;
import com.projet.user.domain.Role;
import com.projet.user.dto.CreateUserCommand;
import com.projet.user.dto.UserAuthView;
import com.projet.user.dto.UserDto;
import com.projet.user.exception.EmailAlreadyExistsException;
import com.projet.user.exception.UserNotFoundException;
import com.projet.user.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final OtpService otpService;
    private final OtpProperties otpProperties;
    private final OtpChannel otpChannel;
    private final PendingRegistrationService pendingRegistrationService;
    private final AuthCookieService authCookieService;

    public RegisterResponse register(RegisterRequest request) {
        userService.findAuthViewByEmail(request.email()).ifPresent(existing -> {
            throw new EmailAlreadyExistsException(request.email(), existing.enabled());
        });

        pendingRegistrationService.save(request.email(), request.firstName(), request.lastName(), request.phone());
        sendOtp(request.email(), request.phone(), request.firstName());

        return new RegisterResponse("Code de vérification envoyé par email.");
    }

    public VerifyOtpResponse verifyOtp(String email, String otp) {
        if (!otpService.verify(email, otp)) {
            throw new InvalidOtpException("Code invalide ou expiré");
        }
        if (pendingRegistrationService.find(email).isEmpty()) {
            throw new InvalidOtpException("Inscription introuvable ou expirée, recommencez.");
        }

        pendingRegistrationService.markVerified(email);
        return new VerifyOtpResponse("Email vérifié. Choisissez votre mot de passe.");
    }

    public UserDto completeRegistration(CompleteRegistrationRequest request, HttpServletResponse response) {
        if (!pendingRegistrationService.isVerified(request.email())) {
            throw new InvalidOtpException("Vérifiez d'abord votre email avant de choisir un mot de passe.");
        }
        PendingRegistration pending = pendingRegistrationService.find(request.email())
                .orElseThrow(() -> new InvalidOtpException("Inscription expirée, recommencez."));

        UserDto user = userService.createUser(new CreateUserCommand(
                request.email(),
                passwordEncoder.encode(request.password()),
                pending.firstName(),
                pending.lastName(),
                pending.phone(),
                Role.USER,
                true
        ));

        pendingRegistrationService.clear(request.email());
        return issueTokensAndGetUser(user.id(), user.email(), user.role().name(), response);
    }

    public void resendOtp(String email) {
        PendingRegistration pending = pendingRegistrationService.find(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        sendOtp(email, pending.phone(), pending.firstName());
    }

    public UserDto login(LoginRequest request, HttpServletResponse response) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.identifier(), request.password()));

        UserAuthView user = userService.findAuthViewByIdentifier(request.identifier())
                .orElseThrow(() -> new InvalidTokenException("Identifiants invalides"));

        return issueTokensAndGetUser(user.id(), user.email(), user.role().name(), response);
    }

    public UserDto refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = authCookieService.readCookie(request, AuthCookieService.REFRESH_COOKIE);
        if (refreshToken == null) {
            throw new InvalidTokenException("Refresh token manquant");
        }

        Claims claims = jwtService.parseAndValidate(refreshToken);
        UUID userId = UUID.fromString(claims.getSubject());

        if (!refreshTokenService.isValid(userId, claims.getId())) {
            refreshTokenService.revoke(userId);
            throw new InvalidTokenException("Refresh token invalide ou déjà utilisé");
        }

        var user = userService.getById(userId);
        refreshTokenService.revoke(userId);
        return issueTokensAndGetUser(user.id(), user.email(), user.role().name(), response);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = authCookieService.readCookie(request, AuthCookieService.ACCESS_COOKIE);
        if (accessToken != null) {
            try {
                Claims claims = jwtService.parseAndValidate(accessToken);
                Duration ttl = Duration.between(Instant.now(), claims.getExpiration().toInstant());
                if (!ttl.isNegative()) {
                    tokenBlacklistService.blacklist(claims.getId(), ttl);
                }
                refreshTokenService.revoke(UUID.fromString(claims.getSubject()));
            } catch (JwtException ignored) {
                // token already invalid/expired : rien à révoquer côté serveur
            }
        }
        authCookieService.clearCookies(response);
    }

    private void sendOtp(String email, String phone, String firstName) {
        String code = otpService.generate(email);
        otpChannel.sendOtp(email, phone, firstName, code, (int) otpProperties.ttlMinutes());
    }

    private UserDto issueTokensAndGetUser(UUID userId, String email, String role, HttpServletResponse response) {
        var access = jwtService.generateAccessToken(userId, email, role);
        var refresh = jwtService.generateRefreshToken(userId);
        refreshTokenService.store(userId, refresh.jti(), Duration.between(Instant.now(), refresh.expiresAt()));

        authCookieService.setAccessCookie(response, access.token(), Duration.between(Instant.now(), access.expiresAt()));
        authCookieService.setRefreshCookie(response, refresh.token(), Duration.between(Instant.now(), refresh.expiresAt()));

        return userService.getById(userId);
    }
}
