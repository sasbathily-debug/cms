package com.projet.integration.notification;

import com.projet.integration.mail.EmailContent;
import com.projet.integration.mail.EmailMessage;
import com.projet.integration.mail.EmailSender;
import com.projet.integration.mail.EmailTemplates;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Envoie le code qui confirme une demande de rendez-vous faite sans compte. Toujours par e-mail, tant qu'aucun
 * fournisseur SMS ou WhatsApp n'est branché (voir {@link OtpChannel} et {@link WhatsAppNotifier}).
 */
@Component
@RequiredArgsConstructor
public class BookingCodeMailer {

    private final EmailSender emailSender;

    public void send(String email, String fullName, String code, int ttlMinutes) {
        EmailContent content = EmailTemplates.bookingCode(fullName, code, ttlMinutes);
        emailSender.send(new EmailMessage(email, content.subject(), content.text(), content.html()));
    }
}
