package com.projet.user.util;

public final class PhoneNumbers {

    private PhoneNumbers() {
    }

    public static String normalize(String raw) {
        return raw == null ? null : raw.replaceAll("[^0-9]", "");
    }
}
