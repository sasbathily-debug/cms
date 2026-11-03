package com.projet.integration.notification;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Le patron Strategy : seuls les canaux nommés dans la configuration envoient, dans cet ordre, et l'un ne bloque pas l'autre. */
class AppointmentNotifierTest {

    private static AppointmentNotice notice() {
        return new AppointmentNotice(AppointmentNotice.Kind.CONFIRMED, "Awa Diop", "+221770000000", "awa@example.com",
                " pour le mardi 10 novembre à 9h00", true, "+221 77 226 39 06", null, "https://exemple.test", true, true);
    }

    private static AppointmentNotificationChannel channel(String name, List<String> journal) {
        return new AppointmentNotificationChannel() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public void send(AppointmentNotice notice) {
                journal.add(name);
            }
        };
    }

    @Test
    void neSertQueLesCanauxActives() {
        List<String> journal = new ArrayList<>();
        AppointmentNotifier notifier = new AppointmentNotifier(
                List.of(channel("email", journal), channel("sms", journal)), List.of("email"));
        notifier.notify(notice());
        assertThat(journal).containsExactly("email");
    }

    @Test
    void respecteLOrdreDeLaConfiguration() {
        List<String> journal = new ArrayList<>();
        AppointmentNotifier notifier = new AppointmentNotifier(
                List.of(channel("email", journal), channel("sms", journal)), List.of(" SMS", "email"));
        notifier.notify(notice());
        assertThat(journal).containsExactly("sms", "email");
    }

    @Test
    void ignoreUnCanalInconnu() {
        List<String> journal = new ArrayList<>();
        AppointmentNotifier notifier = new AppointmentNotifier(List.of(channel("email", journal)), List.of("pigeon", "email"));
        notifier.notify(notice());
        assertThat(journal).containsExactly("email");
    }

    @Test
    void unCanalEnPanneNeBloquePasLesAutres() {
        List<String> journal = new ArrayList<>();
        AppointmentNotificationChannel failing = new AppointmentNotificationChannel() {
            @Override
            public String name() {
                return "sms";
            }

            @Override
            public void send(AppointmentNotice notice) {
                throw new IllegalStateException("fournisseur indisponible");
            }
        };
        AppointmentNotifier notifier = new AppointmentNotifier(List.of(failing, channel("email", journal)), List.of("sms", "email"));
        notifier.notify(notice());
        assertThat(journal).containsExactly("email");
    }
}
