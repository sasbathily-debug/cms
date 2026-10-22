package com.projet.site.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Les champs ajoutés pour le site (catégorie, durée, photo, publication) sont facultatifs : une ancienne
 * interface qui ne les envoie pas ne casse rien. {@code published} absent veut dire « publié ».
 */
public record HealthTipRequest(
        @NotBlank String title,
        @NotBlank String body,
        String icon,
        int displayOrder,
        @Size(max = 60) String category,
        @Min(1) @Max(120) Integer readingMinutes,
        String photoUrl,
        Boolean published
) {
}
