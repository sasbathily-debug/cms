package com.projet.site.dto;

import java.util.UUID;

public record HeroImageDto(
        UUID id,
        String imageUrl,
        int displayOrder
) {
}
