package com.projet.site.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record ReorderHeroImagesRequest(
        @NotEmpty List<UUID> orderedIds
) {
}
