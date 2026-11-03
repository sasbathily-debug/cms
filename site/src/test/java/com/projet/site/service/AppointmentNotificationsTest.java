package com.projet.site.service;

import com.projet.integration.mail.EmailMessage;
import com.projet.integration.mail.EmailSender;
import com.projet.integration.notification.AppointmentNotifier;
import com.projet.integration.notification.EmailAppointmentChannel;
import com.projet.site.domain.Appointment;
import com.projet.site.dto.AppointmentRequest;
import com.projet.site.dto.SiteSettingsDto;
import com.projet.site.mapper.AppointmentMapper;
import com.projet.site.mapper.AppointmentStatusEventMapper;
import com.projet.site.repository.AppointmentRepository;
import com.projet.site.repository.AppointmentStatusEventRepository;
import com.projet.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Du service au canal e-mail : date lisible, et pour un invité un bouton « Déplacer ou annuler » qui écrit au cabinet. */
class AppointmentNotificationsTest {

    private EmailSender emailSender;
    private SiteSettingsService settings;
    private AppointmentServiceImpl service;

    @BeforeEach
    void setUp() {
        emailSender = mock(EmailSender.class);
        settings = mock(SiteSettingsService.class);
        when(settings.get()).thenReturn(new SiteSettingsDto(
                null, null, null, null, null, null, null, null,
                "+221 77 226 39 06", "contact@example.com", "+221 77 226 39 06", null, null, null, null));
        AppointmentRepository repository = mock(AppointmentRepository.class);
        when(repository.saveAndFlush(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));
        service = new AppointmentServiceImpl(
                repository,
                mock(AppointmentStatusEventRepository.class),
                mock(AppointmentMapper.class),
                mock(AppointmentStatusEventMapper.class),
                new AppointmentNotifier(List.of(new EmailAppointmentChannel(emailSender)), List.of("email")),
                mock(UserService.class),
                settings,
                "https://exemple.test");
    }

    private static AppointmentRequest request() {
        return new AppointmentRequest("Awa Diop", "+221770000000", "awa@example.com", "Autre",
                LocalDate.of(2026, 11, 10), LocalTime.of(9, 0), null);
    }

    private EmailMessage sent() {
        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender).send(captor.capture());
        return captor.getValue();
    }

    @Test
    void ecritLaDateEnFrancais() {
        service.createGuest(request(), true);
        assertThat(sent().text()).contains("pour le mardi 10 novembre à 9h00").doesNotContain("2026-11-10");
    }

    @Test
    void unInviteRecoitUnBoutonQuiEcritAuCabinet() {
        service.createGuest(request(), true);
        EmailMessage mail = sent();
        assertThat(mail.html()).contains("Déplacer ou annuler").contains("https://wa.me/221772263906?text=");
        assertThat(mail.html()).doesNotContain("Voir mes rendez-vous");
        assertThat(mail.text()).contains("appelez le cabinet au +221 77 226 39 06");
    }

    @Test
    void unPatientAvecCompteGardeLeLienVersSonEspace() {
        service.create(request(), UUID.randomUUID());
        EmailMessage mail = sent();
        assertThat(mail.html()).contains("Voir mes rendez-vous").contains("https://exemple.test/client");
        assertThat(mail.html()).doesNotContain("wa.me");
    }
}
