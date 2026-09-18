package com.projet.user.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record UpdateProfileRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        String profilePictureUrl,
        String phone,
        LocalDate dateOfBirth,
        boolean emailReminders,
        boolean smsReminders
) {
}
