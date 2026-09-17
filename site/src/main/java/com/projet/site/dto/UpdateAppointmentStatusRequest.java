package com.projet.site.dto;

import com.projet.site.domain.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAppointmentStatusRequest(@NotNull AppointmentStatus status) {
}
