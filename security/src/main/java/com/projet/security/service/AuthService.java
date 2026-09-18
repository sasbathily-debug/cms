package com.projet.security.service;

import com.projet.integration.mail.EmailContent;
import com.projet.integration.mail.EmailMessage;
import com.projet.integration.mail.EmailSender;
import com.projet.integration.mail.EmailTemplates;
import com.projet.security.config.OtpProperties;
import com.projet.security.dto.AuthResponse;
import com.projet.security.dto.LoginRequest;
import com.projet.security.dto.RegisterRequest;
import com.projet.security.dto.RegisterResponse;
import com.projet.security.exception.AccountAlreadyVerifiedException;
import com.projet.security.exception.InvalidOtpException;
import com.projet.security.exception.InvalidTokenException;
import com.projet.security.jwt.JwtService;
import com.projet.user.domain.Role;
import com.projet.user.dto.CreateUserCommand;
import com.projet.user.dto.UserAuthView;
import com.projet.user.dto.UserDto;
import com.projet.user.exception.UserNotFoundException;
import com.projet.user.service.UserService;
import io.jsonwebtoken.Claims;
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
    private final EmailSender emailSender;

    public RegisterResponse register(RegisterRequest request) {
        userService.createUser(new CreateUserCommand(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.firstName(),
                request.lastName(),
                Role.USER,
                false
        ));

        sendOtp(request.email(), request.firstName());

        return new RegisterResponse("Compte créé. Vérifiez votre email pour le code de confirmation.");
    }

    public AuthResponse verifyOtp(String email, String otp) {
        if (!otpService.verify(email, otp)) {
            throw new InvalidOtpException("Code invalide ou expiré");
        }

        UserDto user = userService.confirmEmail(email);
        return issueTokens(user.id(), user.email(), user.role().name());
    }

    public void resendOtp(String email) {
        UserAuthView user = userService.findAuthViewByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        if (user.enabled()) {
            throw new AccountAlreadyVerifiedException(email);
        }

        sendOtp(email, user.firstName());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        UserAuthView user = userService.findAuthViewByEmail(request.email())
                .orElseThrow(() -> new InvalidTokenException("Identifiants invalides"));

        return issueTokens(user.id(), user.email(), user.role().name());
    }

    public AuthResponse refresh(String refreshToken) {
        Claims claims = jwtService.parseAndValidate(refreshToken);
        UUID userId = UUID.fromString(claims.getSubject());

        if (!refreshTokenService.isValid(userId, claims.getId())) {
            refreshTokenService.revoke(userId);
            throw new InvalidTokenException("Refresh token invalide ou déjà utilisé");
        }

        var user = userService.getById(userId);
        refreshTokenService.revoke(userId);
        return issueTokens(user.id(), user.email(), user.role().name());
    }

    public void logout(String accessToken) {
        Claims claims = jwtService.parseAndValidate(accessToken);
        Duration ttl = Duration.between(Instant.now(), claims.getExpiration().toInstant());
        tokenBlacklistService.blacklist(claims.getId(), ttl);
        refreshTokenService.revoke(UUID.fromString(claims.getSubject()));
    }

    private void sendOtp(String email, String firstName) {
        String code = otpService.generate(email);
        EmailContent content = EmailTemplates.otpVerification(firstName, code, (int) otpProperties.ttlMinutes());
        emailSender.send(new EmailMessage(email, content.subject(), content.text(), content.html()));
    }

    private AuthResponse issueTokens(UUID userId, String email, String role) {
        var access = jwtService.generateAccessToken(userId, email, role);
        var refresh = jwtService.generateRefreshToken(userId);
        refreshTokenService.store(userId, refresh.jti(), Duration.between(Instant.now(), refresh.expiresAt()));
        return new AuthResponse(access.token(), refresh.token(), access.expiresAt().getEpochSecond());
    }
}
