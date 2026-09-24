package com.projet.integration.notification;

/**
 * Envoi de notifications WhatsApp, en complément de l'email (pas en remplacement).
 * Implémentation choisie via "app.whatsapp.enabled" (désactivé par défaut).
 * Prêt à activer dès qu'un compte WhatsApp Business API (Meta Cloud API) est configuré.
 */
public interface WhatsAppNotifier {

    void sendAppointmentConfirmed(String phone, String fullName, String dateInfo, String ctaUrl);

    void sendAppointmentReminder(String phone, String fullName, String dateInfo, String ctaUrl);
}
