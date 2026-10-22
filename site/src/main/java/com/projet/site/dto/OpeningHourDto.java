package com.projet.site.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalTime;

/** Horaires d'un jour : {@code dayOfWeek} de 1 (lundi) à 7 (dimanche), heures au format « HH:mm ». */
public record OpeningHourDto(
        @Min(1) @Max(7) int dayOfWeek,
        @JsonFormat(pattern = "HH:mm") LocalTime opens,
        @JsonFormat(pattern = "HH:mm") LocalTime closes,
        boolean closed
) {
}
