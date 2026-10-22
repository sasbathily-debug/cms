package com.projet.site.dto;

import java.util.UUID;

public record FaqItemDto(
        UUID id,
        String question,
        String answer,
        int displayOrder,
        String category,
        boolean published
) {
}
