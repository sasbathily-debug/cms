package com.projet.site.dto;

import com.projet.site.domain.AppointmentStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AppointmentDto(
        UUID id,
        String fullName,
        String phone,
        String email,
        String reason,
        LocalDate preferredDate,
        String message,
        UUID userId,
        AppointmentStatus status,
        Instant createdAt
) {
}
