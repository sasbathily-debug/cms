package com.projet.site.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Message du formulaire de contact du site. {@code website} est un piège à robots : champ caché que
 * les visiteurs ne remplissent jamais ; s'il est rempli, le message est ignoré sans bruit.
 */
public record ContactMessageRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 30) String phone,
        @Email @Size(max = 150) String email,
        @Size(max = 80) String motif,
        @NotBlank @Size(max = 4000) String message,
        @AssertTrue(message = "Le consentement est requis") boolean consent,
        @Size(max = 200) String website
) {
}
