package com.projet.site.dto;

import java.util.UUID;

public record CareServiceDto(UUID id, String title, String description, String icon, int displayOrder) {
}
