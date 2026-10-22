package com.projet.integration.mail;

/**
 * Un email à envoyer. {@code replyTo} est facultatif : pour les messages du formulaire de contact,
 * il permet à la doctoresse de répondre directement au visiteur.
 */
public record EmailMessage(String to, String subject, String text, String html, String replyTo) {

    public EmailMessage(String to, String subject, String text, String html) {
        this(to, subject, text, html, null);
    }
}
