package com.projet.site.exception;

/** Trop de messages envoyés depuis la même adresse (ou globalement) sur la période. */
public class ContactRateLimitException extends RuntimeException {

    public ContactRateLimitException(String message) {
        super(message);
    }
}
