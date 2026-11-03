package com.projet.integration.notification;

/**
 * Un canal pour prévenir un patient (e-mail, SMS, WhatsApp…). C'est la « stratégie » : {@link AppointmentNotifier}
 * ne connaît que cette interface. Pour ajouter un canal, on écrit une classe qui l'implémente ; pour le brancher ou
 * le débrancher, on change {@code app.notifications.channels}, sans toucher au reste du code.
 */
public interface AppointmentNotificationChannel {

    /** Nom du canal dans la configuration (« email », « sms », « whatsapp »). */
    String name();

    /**
     * Envoie le message. Le canal décide lui-même s'il s'applique (adresse manquante, patient qui a refusé ce canal) :
     * dans ce cas il ne fait rien.
     */
    void send(AppointmentNotice notice);
}
