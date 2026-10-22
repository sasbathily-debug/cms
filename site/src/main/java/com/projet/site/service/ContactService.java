package com.projet.site.service;

import com.projet.integration.mail.EmailContent;
import com.projet.integration.mail.EmailMessage;
import com.projet.integration.mail.EmailSender;
import com.projet.integration.mail.EmailTemplates;
import com.projet.site.dto.ContactMessageRequest;
import com.projet.site.exception.ContactRateLimitException;
import com.projet.site.exception.ContactUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Remet par e-mail à la doctoresse un message écrit dans le formulaire de contact du site.
 * Chaque envoi est un e-mail (jamais un SMS) : aucun crédit de SMS n'est consommé.
 */
@Slf4j
@Service
public class ContactService {

    private static final String DEFAULT_MOTIF = "Autre";

    private final EmailSender emailSender;
    private final SiteSettingsService siteSettingsService;
    private final ContactRateLimiter rateLimiter;
    private final String fallbackRecipient;

    public ContactService(
            EmailSender emailSender,
            SiteSettingsService siteSettingsService,
            ContactRateLimiter rateLimiter,
            @Value("${app.contact.to:}") String fallbackRecipient) {
        this.emailSender = emailSender;
        this.siteSettingsService = siteSettingsService;
        this.rateLimiter = rateLimiter;
        this.fallbackRecipient = fallbackRecipient;
    }

    public void send(ContactMessageRequest request, String clientIp) {
        // Piège à robots : on répond comme si tout allait bien, sans rien envoyer.
        if (StringUtils.hasText(request.website())) {
            log.info("Message de contact ignoré (champ piège rempli) depuis {}", clientIp);
            return;
        }

        if (!rateLimiter.tryAcquire(clientIp)) {
            throw new ContactRateLimitException(
                    "Trop de messages envoyés. Réessayez plus tard ou appelez le cabinet.");
        }

        String recipient = resolveRecipient();
        String name = singleLine(request.name());
        String phone = singleLine(request.phone());
        String email = singleLine(request.email());
        String motif = StringUtils.hasText(request.motif()) ? singleLine(request.motif()) : DEFAULT_MOTIF;
        String message = request.message().strip();

        EmailContent content = EmailTemplates.contactMessage(name, phone, email, motif, message);
        String replyTo = StringUtils.hasText(email) ? email : null;
        try {
            emailSender.send(new EmailMessage(recipient, content.subject(), content.text(), content.html(), replyTo));
        } catch (RuntimeException e) {
            log.error("Échec de l'envoi du message de contact : {}", e.getMessage());
            throw new ContactUnavailableException("Le message n'a pas pu être envoyé.", e);
        }
    }

    private String resolveRecipient() {
        String configured = siteSettingsService.get().email();
        if (StringUtils.hasText(configured)) {
            return configured.strip();
        }
        if (StringUtils.hasText(fallbackRecipient)) {
            return fallbackRecipient.strip();
        }
        throw new ContactUnavailableException("Aucune adresse de réception n'est configurée.");
    }

    /** Retire sauts de ligne et caractères de contrôle des champs d'une ligne (sujet, en-têtes). */
    static String singleLine(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[\\p{Cntrl}]+", " ").strip();
    }
}
