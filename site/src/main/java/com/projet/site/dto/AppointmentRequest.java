package com.projet.site.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentRequest(
        @NotBlank String fullName,
        @NotBlank String phone,
        @Email String email,
        @NotBlank String reason,
        LocalDate preferredDate,
        LocalTime preferredTime,
        String message
) {
}
