package com.projet.site.dto;

import jakarta.validation.constraints.NotBlank;

public record FaqItemRequest(
        @NotBlank String question,
        @NotBlank String answer,
        int displayOrder
) {
}
