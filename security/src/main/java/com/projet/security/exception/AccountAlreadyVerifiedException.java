package com.projet.security.exception;

public class AccountAlreadyVerifiedException extends RuntimeException {

    public AccountAlreadyVerifiedException(String email) {
        super("Le compte " + email + " est déjà vérifié");
    }
}
