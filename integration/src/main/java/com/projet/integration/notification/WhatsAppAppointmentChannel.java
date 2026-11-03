package com.projet.integration.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** Canal WhatsApp : s'appuie sur {@link WhatsAppNotifier} (sans effet tant que WhatsApp n'est pas configuré). */
@Component
@RequiredArgsConstructor
public class WhatsAppAppointmentChannel implements AppointmentNotificationChannel {

    public static final String NAME = "whatsapp";

    private final WhatsAppNotifier whatsAppNotifier;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void send(AppointmentNotice notice) {
        if (!notice.wantsPhoneMessage() || !StringUtils.hasText(notice.phone())) {
            return;
        }
        String ctaUrl = notice.appUrl() + "/client";
        switch (notice.kind()) {
            case CONFIRMED -> whatsAppNotifier.sendAppointmentConfirmed(notice.phone(), notice.fullName(), notice.dateInfo(), ctaUrl);
            case REMINDER -> whatsAppNotifier.sendAppointmentReminder(notice.phone(), notice.fullName(), notice.dateInfo(), ctaUrl);
            case CANCELLED -> {
                // Pas de modèle d'annulation WhatsApp pour le moment.
            }
        }
    }
}
