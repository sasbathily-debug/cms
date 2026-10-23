package com.projet.security.controller;

import com.projet.security.dto.CompleteRegistrationRequest;
import com.projet.security.dto.ForgotPasswordRequest;
import com.projet.security.dto.LoginRequest;
import com.projet.security.dto.RegisterRequest;
import com.projet.security.dto.RegisterResponse;
import com.projet.security.dto.ResendOtpRequest;
import com.projet.security.dto.ResetPasswordRequest;
import com.projet.security.dto.VerifyOtpRequest;
import com.projet.security.dto.VerifyOtpResponse;
import com.projet.security.service.AuthService;
import com.projet.user.dto.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/verify-otp")
    public VerifyOtpResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return authService.verifyOtp(request.email(), request.otp());
    }

    @PostMapping("/complete-registration")
    public UserDto completeRegistration(@Valid @RequestBody CompleteRegistrationRequest request,
                                         HttpServletResponse response) {
        return authService.completeRegistration(request, response);
    }

    @PostMapping("/resend-otp")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        authService.resendOtp(request.email());
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.email());
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
    }

    @PostMapping("/login")
    public UserDto login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        return authService.login(request, response);
    }

    @PostMapping("/refresh")
    public UserDto refresh(HttpServletRequest request, HttpServletResponse response) {
        return authService.refresh(request, response);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
    }
}
