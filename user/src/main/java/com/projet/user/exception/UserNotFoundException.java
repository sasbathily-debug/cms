package com.projet.user.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID id) {
        super("Utilisateur introuvable : " + id);
    }

    public UserNotFoundException(String email) {
        super("Utilisateur introuvable : " + email);
    }
}
