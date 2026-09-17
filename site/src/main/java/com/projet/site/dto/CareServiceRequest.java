package com.projet.site.dto;

import jakarta.validation.constraints.NotBlank;

public record CareServiceRequest(
        @NotBlank String title,
        @NotBlank String description,
        String icon,
        int displayOrder
) {
}
