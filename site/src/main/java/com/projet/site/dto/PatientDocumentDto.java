package com.projet.site.dto;

import java.time.Instant;
import java.util.UUID;

public record PatientDocumentDto(
        UUID id,
        UUID userId,
        UUID appointmentId,
        String fileName,
        String fileUrl,
        String category,
        Instant uploadedAt
) {
}
