package com.projet.site.dto;

import jakarta.validation.constraints.NotBlank;

public record HealthTipRequest(
        @NotBlank String title,
        @NotBlank String body,
        String icon,
        int displayOrder
) {
}
