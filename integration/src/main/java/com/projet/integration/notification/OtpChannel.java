package com.projet.integration.notification;

/**
 * Stratégie d'envoi du code OTP. Implémentation choisie via "app.otp.channel"
 * (email par défaut, sms une fois un fournisseur SMS configuré).
 */
public interface OtpChannel {

    void sendOtp(String email, String phone, String firstName, String code, int ttlMinutes);
}
