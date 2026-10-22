package com.projet.site.dto;

import tools.jackson.databind.JsonNode;

import java.time.Instant;

/** Une page : son identifiant, son contenu (document JSON) et la date de la dernière modification. */
public record SitePageDto(String slug, JsonNode content, Instant updatedAt) {
}
