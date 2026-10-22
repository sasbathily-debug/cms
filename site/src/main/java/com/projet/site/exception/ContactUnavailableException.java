package com.projet.site.exception;

/** Le message n'a pas pu être remis (pas d'adresse de destination, ou échec de l'envoi). */
public class ContactUnavailableException extends RuntimeException {

    public ContactUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContactUnavailableException(String message) {
        super(message);
    }
}
