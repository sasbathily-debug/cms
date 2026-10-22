package com.projet.site.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Les sept jours de la semaine, dans n'importe quel ordre. */
public record UpdateOpeningHoursRequest(
        @NotNull @Size(min = 7, max = 7) List<@Valid OpeningHourDto> days
) {
}
