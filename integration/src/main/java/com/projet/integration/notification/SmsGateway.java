package com.projet.integration.notification;

/**
 * Le fournisseur de SMS (Orange, Twilio, un agrégateur local…). Une seule méthode : pour brancher un fournisseur, on
 * écrit une classe qui l'implémente et on la sélectionne avec {@code app.sms.provider}.
 */
public interface SmsGateway {

    void send(String phone, String text);
}
