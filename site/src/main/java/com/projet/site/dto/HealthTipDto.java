package com.projet.site.dto;

import java.util.UUID;

public record HealthTipDto(UUID id, String title, String body, String icon, int displayOrder) {
}
