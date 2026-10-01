package com.projet.site.dto;

public record AppointmentCountsDto(
        long all,
        long confirmed,
        long cancelled,
        long completed,
        long noShow
) {
}
