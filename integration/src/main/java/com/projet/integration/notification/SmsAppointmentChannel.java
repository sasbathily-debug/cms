package com.projet.integration.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Canal SMS : prêt à l'emploi, mais il n'envoie rien tant que {@code sms} n'est pas dans
 * {@code app.notifications.channels} ET qu'un {@link SmsGateway} réel est branché. Les messages sont courts
 * (une page de SMS, environ 160 caractères).
 */
@Component
@RequiredArgsConstructor
public class SmsAppointmentChannel implements AppointmentNotificationChannel {

    public static final String NAME = "sms";

    private final SmsGateway gateway;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void send(AppointmentNotice notice) {
        if (!notice.wantsPhoneMessage() || !StringUtils.hasText(notice.phone())) {
            return;
        }
        gateway.send(notice.phone(), text(notice));
    }

    static String text(AppointmentNotice notice) {
        String contact = StringUtils.hasText(notice.cabinetPhone()) ? " Pour déplacer ou annuler : " + notice.cabinetPhone() + "." : "";
        return switch (notice.kind()) {
            case CONFIRMED -> "Cabinet Dr Fall : rendez-vous confirmé" + notice.dateInfo() + "." + (notice.guest() ? contact : "");
            case REMINDER -> "Cabinet Dr Fall : rappel, votre rendez-vous a lieu demain" + notice.dateInfo() + "." + (notice.guest() ? contact : "");
            case CANCELLED -> "Cabinet Dr Fall : votre rendez-vous a été annulé." + (StringUtils.hasText(notice.cabinetPhone())
                    ? " Pour reprogrammer : " + notice.cabinetPhone() + "." : "");
        };
    }
}
