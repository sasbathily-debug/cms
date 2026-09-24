package com.projet.site.exception;

public class SlotAlreadyBookedException extends RuntimeException {

    public SlotAlreadyBookedException() {
        super("Ce créneau vient d'être réservé par quelqu'un d'autre. Merci d'en choisir un autre.");
    }
}
