package com.projet.site.dto;

import com.projet.site.domain.AppointmentStatus;

import java.time.Instant;
import java.util.UUID;

public record AppointmentStatusEventDto(
        UUID id,
        AppointmentStatus previousStatus,
        AppointmentStatus newStatus,
        Instant createdAt
) {
}
