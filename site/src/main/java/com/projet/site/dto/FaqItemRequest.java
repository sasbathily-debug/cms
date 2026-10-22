package com.projet.site.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** {@code category} (le thème) et {@code published} sont facultatifs ; {@code published} absent veut dire « publié ». */
public record FaqItemRequest(
        @NotBlank String question,
        @NotBlank String answer,
        int displayOrder,
        @Size(max = 60) String category,
        Boolean published
) {
}
