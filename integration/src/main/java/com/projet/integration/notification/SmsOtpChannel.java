package com.projet.integration.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Prêt à activer dès qu'un fournisseur SMS (pack acheté + clés API) est branché ici :
 * implémenter l'appel réel au fournisseur puis passer app.otp.channel=sms.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.otp", name = "channel", havingValue = "sms")
public class SmsOtpChannel implements OtpChannel {

    @Override
    public void sendOtp(String email, String phone, String firstName, String code, int ttlMinutes) {
        log.error("app.otp.channel=sms mais aucun fournisseur SMS n'est encore branché (destinataire : {})", phone);
        throw new IllegalStateException("Aucun fournisseur SMS n'est configuré pour l'envoi du code de vérification.");
    }
}
