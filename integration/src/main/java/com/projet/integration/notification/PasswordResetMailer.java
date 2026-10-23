package com.projet.integration.notification;

import com.projet.integration.mail.EmailContent;
import com.projet.integration.mail.EmailMessage;
import com.projet.integration.mail.EmailSender;
import com.projet.integration.mail.EmailTemplates;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Envoie le code de réinitialisation du mot de passe. Toujours par e-mail, quel que soit le canal choisi pour
 * le code d'inscription (app.otp.channel) : la réinitialisation ne doit pas dépendre d'un fournisseur SMS.
 */
@Component
@RequiredArgsConstructor
public class PasswordResetMailer {

    private final EmailSender emailSender;

    public void send(String email, String firstName, String code, int ttlMinutes) {
        EmailContent content = EmailTemplates.passwordReset(firstName, code, ttlMinutes);
        emailSender.send(new EmailMessage(email, content.subject(), content.text(), content.html()));
    }
}
