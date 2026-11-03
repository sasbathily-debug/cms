package com.projet.integration.notification;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class SmsAppointmentChannelTest {

    private static AppointmentNotice notice(AppointmentNotice.Kind kind, boolean guest, boolean wantsPhoneMessage) {
        return new AppointmentNotice(kind, "Awa Diop", "+221770000000", null, " pour le mardi 10 novembre à 9h00",
                guest, "+221 77 226 39 06", null, "https://exemple.test", false, wantsPhoneMessage);
    }

    @Test
    void envoieUnSmsCourtParLeFournisseurBranche() {
        SmsGateway gateway = mock(SmsGateway.class);
        new SmsAppointmentChannel(gateway).send(notice(AppointmentNotice.Kind.CONFIRMED, true, true));
        verify(gateway).send(eq("+221770000000"), any());
    }

    @Test
    void lesMessagesTiennentDansUnSms() {
        for (AppointmentNotice.Kind kind : AppointmentNotice.Kind.values()) {
            assertThat(SmsAppointmentChannel.text(notice(kind, true, true)).length()).as(kind.name()).isLessThanOrEqualTo(160);
        }
        assertThat(SmsAppointmentChannel.text(notice(AppointmentNotice.Kind.CONFIRMED, true, true)))
                .contains("mardi 10 novembre à 9h00").contains("+221 77 226 39 06");
    }

    @Test
    void neFaitRienSiLePatientRefuseLesMessagesTelephone() {
        SmsGateway gateway = mock(SmsGateway.class);
        new SmsAppointmentChannel(gateway).send(notice(AppointmentNotice.Kind.REMINDER, false, false));
        verify(gateway, never()).send(any(), any());
    }
}
