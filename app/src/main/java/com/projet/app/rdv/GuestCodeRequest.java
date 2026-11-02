package com.projet.app.rdv;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Demande du code de confirmation d'un rendez-vous pris sans compte (envoyé par e-mail). */
public record GuestCodeRequest(
        @NotBlank @Email @Size(max = 160) String email,
        @NotBlank @Size(max = 120) String fullName,
        String website
) {
}
