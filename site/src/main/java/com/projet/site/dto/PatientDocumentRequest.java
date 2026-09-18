package com.projet.site.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PatientDocumentRequest(
        @NotNull UUID userId,
        UUID appointmentId,
        @NotBlank String fileName,
        @NotBlank String fileUrl,
        @NotBlank String category
) {
}
