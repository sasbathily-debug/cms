package com.projet.integration.notification;

/**
 * Tout ce qu'un canal a besoin de savoir pour prévenir un patient d'un rendez-vous, sans rien connaître du reste de
 * l'application. Le service de rendez-vous le remplit une fois ; chaque canal en tire son message.
 *
 * @param dateInfo          « pour le mardi 10 novembre à 9h00 » (ou vide)
 * @param guest             vrai si le rendez-vous a été pris sans compte
 * @param cabinetPhone      numéro du cabinet, pour « déplacer ou annuler »
 * @param cabinetWhatsapp   numéro WhatsApp du cabinet (chiffres ou format libre)
 * @param appUrl            adresse du site
 * @param wantsEmail        le patient accepte les e-mails (réglage de son compte ; vrai pour un invité)
 * @param wantsPhoneMessage le patient accepte les messages sur son téléphone (SMS, WhatsApp)
 */
public record AppointmentNotice(
        Kind kind,
        String fullName,
        String phone,
        String email,
        String dateInfo,
        boolean guest,
        String cabinetPhone,
        String cabinetWhatsapp,
        String appUrl,
        boolean wantsEmail,
        boolean wantsPhoneMessage) {

    public enum Kind {
        CONFIRMED, REMINDER, CANCELLED
    }
}
