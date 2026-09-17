package com.projet.integration.mail;

/**
 * Stratégie d'envoi d'email. L'implémentation active est choisie via la propriété
 * "app.mail.provider" (log par défaut, smtp si configuré) sans toucher au code appelant.
 */
public interface EmailSender {

    void send(EmailMessage message);
}
