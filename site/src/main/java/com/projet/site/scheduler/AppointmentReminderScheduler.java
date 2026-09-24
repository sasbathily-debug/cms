package com.projet.site.scheduler;

import com.projet.site.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentReminderScheduler {

    private final AppointmentService appointmentService;

    @Scheduled(cron = "0 0 8 * * *")
    public void sendDailyReminders() {
        log.info("Démarrage de l'envoi des rappels de rendez-vous du lendemain");
        appointmentService.sendDueReminders();
    }
}
