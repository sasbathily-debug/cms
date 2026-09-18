package com.projet.site.exception;

import java.time.LocalDate;

public class DuplicateScheduleExceptionException extends RuntimeException {

    public DuplicateScheduleExceptionException(LocalDate date) {
        super("Une exception existe déjà pour le " + date);
    }
}
