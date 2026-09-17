package com.projet.integration.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Stratégie par défaut : n'envoie rien, log le contenu. Active tant que
 * app.mail.provider n'est pas explicitement mis à "smtp" — utile pour développer
 * sans avoir de vraies clés SMTP.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.mail", name = "provider", havingValue = "log", matchIfMissing = true)
public class LoggingEmailSender implements EmailSender {

    @Override
    public void send(EmailMessage message) {
        log.info("[MAIL:LOG] to={} subject=\"{}\" body=\"{}\"", message.to(), message.subject(), message.body());
    }
}
