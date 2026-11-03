package com.projet.integration.notification;

import com.projet.integration.mail.EmailMessage;
import com.projet.integration.mail.EmailSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class EmailAppointmentChannelTest {

    private EmailSender sender;
    private EmailAppointmentChannel channel;

    @BeforeEach
    void setUp() {
        sender = mock(EmailSender.class);
        channel = new EmailAppointmentChannel(sender);
    }

    private static AppointmentNotice notice(AppointmentNotice.Kind kind, boolean guest, String email, boolean wantsEmail) {
        return new AppointmentNotice(kind, "Awa Diop", "+221770000000", email, " pour le mardi 10 novembre à 9h00",
                guest, "+221 77 226 39 06", "+221 77 226 39 06", "https://exemple.test", wantsEmail, true);
    }

    private EmailMessage sent() {
        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(sender).send(captor.capture());
        return captor.getValue();
    }

    @Test
    void invitePrevenuParUnBoutonQuiEcritAuCabinet() {
        channel.send(notice(AppointmentNotice.Kind.CONFIRMED, true, "awa@example.com", true));
        EmailMessage mail = sent();
        assertThat(mail.text()).contains("pour le mardi 10 novembre à 9h00").contains("appelez le cabinet au +221 77 226 39 06");
        assertThat(mail.html()).contains("Déplacer ou annuler").contains("https://wa.me/221772263906?text=").doesNotContain("Voir mes rendez-vous");
    }

    @Test
    void patientAvecCompteGardeLeLienVersSonEspace() {
        channel.send(notice(AppointmentNotice.Kind.REMINDER, false, "awa@example.com", true));
        assertThat(sent().html()).contains("Voir mes rendez-vous").contains("https://exemple.test/client").doesNotContain("wa.me");
    }

    @Test
    void annulationSansBoutonDeMessage() {
        channel.send(notice(AppointmentNotice.Kind.CANCELLED, true, "awa@example.com", true));
        assertThat(sent().subject()).isEqualTo("Votre rendez-vous a été annulé");
    }

    @Test
    void neFaitRienSansAdresseOuSiLePatientNeVeutPasDEmail() {
        channel.send(notice(AppointmentNotice.Kind.CONFIRMED, true, null, true));
        channel.send(notice(AppointmentNotice.Kind.CONFIRMED, false, "awa@example.com", false));
        verify(sender, never()).send(any());
    }
}
