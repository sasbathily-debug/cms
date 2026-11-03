package com.projet.integration.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Le « contexte » du patron Strategy : il envoie chaque message par tous les canaux activés, dans l'ordre de la
 * configuration {@code app.notifications.channels} (par défaut : e-mail seulement). Un canal qui échoue n'empêche pas
 * les autres, ni la création du rendez-vous.
 */
@Slf4j
@Service
public class AppointmentNotifier {

    private final List<AppointmentNotificationChannel> active = new ArrayList<>();

    public AppointmentNotifier(
            List<AppointmentNotificationChannel> available,
            @Value("${app.notifications.channels:email}") List<String> enabled) {
        for (String raw : enabled) {
            String name = raw.trim().toLowerCase(Locale.ROOT);
            if (name.isEmpty()) {
                continue;
            }
            available.stream()
                    .filter(channel -> channel.name().equals(name))
                    .findFirst()
                    .ifPresentOrElse(active::add,
                            () -> log.warn("Canal de notification inconnu dans app.notifications.channels : « {} »", name));
        }
        log.info("Canaux de notification des rendez-vous : {}", active.stream().map(AppointmentNotificationChannel::name).toList());
    }

    public void notify(AppointmentNotice notice) {
        for (AppointmentNotificationChannel channel : active) {
            try {
                channel.send(notice);
            } catch (RuntimeException e) {
                log.warn("Échec du canal {} pour le rendez-vous de {} : {}", channel.name(), notice.fullName(), e.getMessage());
            }
        }
    }
}
