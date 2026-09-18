package com.projet.user.exception;

public class InvalidCurrentPasswordException extends RuntimeException {

    public InvalidCurrentPasswordException() {
        super("Mot de passe actuel incorrect");
    }
}
