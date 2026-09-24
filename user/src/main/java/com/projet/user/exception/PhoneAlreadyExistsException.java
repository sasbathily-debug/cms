package com.projet.user.exception;

public class PhoneAlreadyExistsException extends RuntimeException {

    public PhoneAlreadyExistsException(String phone) {
        super("Un compte existe déjà avec le numéro : " + phone);
    }
}
