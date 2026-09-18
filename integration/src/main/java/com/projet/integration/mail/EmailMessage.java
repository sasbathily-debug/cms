package com.projet.integration.mail;

public record EmailMessage(String to, String subject, String text, String html) {
}
