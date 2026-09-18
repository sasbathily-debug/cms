package com.projet.site.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record AvailableSlotDto(LocalDate date, LocalTime time) {
}
