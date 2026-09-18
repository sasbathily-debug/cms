package com.projet.integration.mail;

/**
 * Corps d'un email dans ses deux variantes : texte brut (fallback) et HTML
 * (rendu réel dans la plupart des clients mail).
 */
public record EmailContent(String subject, String text, String html) {
}
