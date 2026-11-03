package com.projet.integration.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Aucun fournisseur de SMS branché : on le note dans les journaux et on n'envoie rien. */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.sms", name = "provider", havingValue = "none", matchIfMissing = true)
public class NoOpSmsGateway implements SmsGateway {

    @Override
    public void send(String phone, String text) {
        log.info("SMS non configuré — message non envoyé à {}", phone);
    }
}
