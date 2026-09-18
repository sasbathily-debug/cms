package com.projet.site.exception;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class DuplicateAvailabilityRuleException extends RuntimeException {

    public DuplicateAvailabilityRuleException(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        super("Une règle identique existe déjà pour " + dayOfWeek + " de " + startTime + " à " + endTime);
    }
}
