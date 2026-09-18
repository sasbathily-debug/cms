package com.projet.site.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ScheduleExceptionDto(
        UUID id,
        LocalDate date,
        boolean closed,
        LocalTime customStartTime,
        LocalTime customEndTime,
        Integer slotDurationMinutes,
        String note
) {
}
