package com.projet.site.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleExceptionRequest(
        @NotNull LocalDate date,
        boolean closed,
        LocalTime customStartTime,
        LocalTime customEndTime,
        Integer slotDurationMinutes,
        String note
) {
}
