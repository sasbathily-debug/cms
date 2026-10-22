package com.projet.site.dto;

import java.time.Instant;

/** Une page modifiée depuis l'admin, sans son contenu (pour la liste de l'admin). */
public record SitePageSummaryDto(String slug, Instant updatedAt) {
}
