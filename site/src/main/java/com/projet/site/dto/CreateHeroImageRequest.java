package com.projet.site.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateHeroImageRequest(
        @NotBlank String imageUrl,
        String title
) {
}
