package com.projet.integration.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.whatsapp", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoOpWhatsAppNotifier implements WhatsAppNotifier {

    @Override
    public void sendAppointmentConfirmed(String phone, String fullName, String dateInfo, String ctaUrl) {
        log.info("WhatsApp non configuré — confirmation non envoyée à {}", phone);
    }
}
