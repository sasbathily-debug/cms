package com.projet.site.dto;

import com.projet.site.domain.AppointmentStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentDto(
        UUID id,
        String fullName,
        String phone,
        String email,
        String reason,
        LocalDate preferredDate,
        LocalTime preferredTime,
        String message,
        UUID userId,
        AppointmentStatus status,
        boolean contactVerified,
        Instant createdAt
) {
}
