package com.projet.site.service;

import com.projet.integration.mail.EmailMessage;
import com.projet.integration.mail.EmailSender;
import com.projet.site.dto.ContactMessageRequest;
import com.projet.site.dto.SiteSettingsDto;
import com.projet.site.exception.ContactRateLimitException;
import com.projet.site.exception.ContactUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ContactServiceTest {

    private EmailSender emailSender;
    private SiteSettingsService settings;
    private ContactService service;

    @BeforeEach
    void setUp() {
        emailSender = mock(EmailSender.class);
        settings = mock(SiteSettingsService.class);
        when(settings.get()).thenReturn(settingsWithEmail("cabinet@example.com"));
        service = new ContactService(emailSender, settings, new ContactRateLimiter(), "");
    }

    private static SiteSettingsDto settingsWithEmail(String email) {
        return new SiteSettingsDto("t", null, null, null, null, null, null, null, null, email, null, null, null, null, null);
    }

    private static ContactMessageRequest request(String email, String website) {
        return new ContactMessageRequest("Awa Diop", "+221770000000", email, "Rendez-vous",
                "Bonjour,\nje voudrais un rendez-vous.", true, website);
    }

    @Test
    void envoieLeMessageAuCabinetAvecLaReponseVersLeVisiteur() {
        service.send(request("awa@example.com", null), "1.1.1.1");

        ArgumentCaptor<EmailMessage> sent = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender).send(sent.capture());
        assertThat(sent.getValue().to()).isEqualTo("cabinet@example.com");
        assertThat(sent.getValue().replyTo()).isEqualTo("awa@example.com");
        assertThat(sent.getValue().subject()).contains("Rendez-vous").contains("Awa Diop");
        assertThat(sent.getValue().text()).contains("+221770000000").contains("je voudrais un rendez-vous");
    }

    @Test
    void sansEmailDuVisiteurPasDeReponseVers() {
        service.send(request("", null), "1.1.1.1");

        ArgumentCaptor<EmailMessage> sent = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender).send(sent.capture());
        assertThat(sent.getValue().replyTo()).isNull();
    }

    @Test
    void lePiegeARobotsEtouffeLeMessageSansErreur() {
        service.send(request("awa@example.com", "http://spam.example"), "1.1.1.1");

        verify(emailSender, never()).send(any());
    }

    @Test
    void refuseApresTropDeMessagesDeLaMemeAdresse() {
        for (int i = 0; i < ContactRateLimiter.PER_IP_LIMIT; i++) {
            service.send(request("awa@example.com", null), "1.1.1.1");
        }

        assertThatThrownBy(() -> service.send(request("awa@example.com", null), "1.1.1.1"))
                .isInstanceOf(ContactRateLimitException.class);
    }

    @Test
    void neLaissePasPasserDeSautsDeLigneDansLeSujet() {
        ContactMessageRequest injection = new ContactMessageRequest("Awa\r\nBcc: spam@example.com", "+221770000000",
                "awa@example.com", "Autre", "Bonjour", true, null);

        service.send(injection, "1.1.1.1");

        ArgumentCaptor<EmailMessage> sent = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender).send(sent.capture());
        assertThat(sent.getValue().subject()).doesNotContain("\r").doesNotContain("\n");
    }

    @Test
    void utiliseLAdresseDeRepliSiLeCabinetNEnAPas() {
        when(settings.get()).thenReturn(settingsWithEmail(""));
        ContactService withFallback = new ContactService(emailSender, settings, new ContactRateLimiter(), "repli@example.com");

        withFallback.send(request(null, null), "1.1.1.1");

        ArgumentCaptor<EmailMessage> sent = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender).send(sent.capture());
        assertThat(sent.getValue().to()).isEqualTo("repli@example.com");
    }

    @Test
    void echoueProprementSiAucuneAdresseNEstConfiguree() {
        when(settings.get()).thenReturn(settingsWithEmail(null));

        assertThatThrownBy(() -> service.send(request(null, null), "1.1.1.1"))
                .isInstanceOf(ContactUnavailableException.class);
    }

    @Test
    void signaleUnEchecDEnvoiAuLieuDeFaireSembleurQueCaMarche() {
        doThrow(new RuntimeException("smtp down")).when(emailSender).send(any());

        assertThatThrownBy(() -> service.send(request("awa@example.com", null), "1.1.1.1"))
                .isInstanceOf(ContactUnavailableException.class);
    }
}
