package com.projet.site.dto;

import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.JsonNode;

/** Le contenu complet de la page, sous forme d'objet JSON. */
public record UpdateSitePageRequest(@NotNull JsonNode content) {
}
