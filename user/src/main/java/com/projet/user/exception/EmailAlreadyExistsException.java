package com.projet.user.exception;

import lombok.Getter;

@Getter
public class EmailAlreadyExistsException extends RuntimeException {

    private final boolean accountVerified;

    public EmailAlreadyExistsException(String email, boolean accountVerified) {
        super("Un compte existe déjà avec l'email : " + email);
        this.accountVerified = accountVerified;
    }
}
