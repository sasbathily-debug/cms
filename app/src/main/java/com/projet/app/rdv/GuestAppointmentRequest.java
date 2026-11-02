package com.projet.app.rdv;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Demande de rendez-vous sans compte. Avec une adresse e-mail, le patient doit saisir le code reçu à cette adresse
 * (preuve que l'adresse est la sienne : le rappel du rendez-vous lui parviendra). Sans adresse e-mail, le rendez-vous est
 * enregistré « non vérifié » et le cabinet confirme par WhatsApp ou par téléphone. `website` est un champ piège pour
 * les robots : un humain ne le voit pas et le laisse vide.
 */
public record GuestAppointmentRequest(
        @NotBlank @Size(max = 120) String fullName,
        @NotBlank @Size(max = 40) String phone,
        @Email @Size(max = 160) String email,
        @NotBlank @Size(max = 160) String reason,
        @NotNull LocalDate preferredDate,
        @NotNull LocalTime preferredTime,
        @Size(max = 1000) String message,
        @Size(max = 12) String code,
        String website
) {
}
