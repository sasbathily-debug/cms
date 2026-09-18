package com.projet.integration.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Envoie les emails via l'API HTTP de Brevo plutôt que du SMTP brut : les hébergeurs
 * cloud (Render inclus) bloquent souvent les ports SMTP sortants (25/465/587) pour
 * lutter contre le spam, mais laissent toujours passer le HTTPS (443).
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.mail", name = "provider", havingValue = "brevo")
public class BrevoEmailSender implements EmailSender {

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    private final RestClient restClient;
    private final String senderEmail;
    private final String senderName;

    public BrevoEmailSender(
            @Value("${app.mail.brevo.api-key}") String apiKey,
            @Value("${app.mail.brevo.sender-email}") String senderEmail,
            @Value("${app.mail.brevo.sender-name}") String senderName) {
        this.senderEmail = senderEmail;
        this.senderName = senderName;
        this.restClient = RestClient.builder()
                .baseUrl(BREVO_API_URL)
                .defaultHeader("api-key", apiKey)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Override
    public void send(EmailMessage message) {
        Map<String, Object> body = Map.of(
                "sender", Map.of("email", senderEmail, "name", senderName),
                "to", List.of(Map.of("email", message.to())),
                "subject", message.subject(),
                "textContent", message.text(),
                "htmlContent", message.html()
        );

        restClient.post()
                .body(body)
                .retrieve()
                .toBodilessEntity();

        log.info("Email envoyé via Brevo à {}", message.to());
    }
}
