package com.projet.integration.notification;

import com.projet.integration.mail.EmailContent;
import com.projet.integration.mail.EmailMessage;
import com.projet.integration.mail.EmailSender;
import com.projet.integration.mail.EmailTemplates;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Canal e-mail. Le bouton du message dépend du patient : l'espace patient pour qui a un compte, un message WhatsApp
 * prérempli au cabinet pour qui n'en a pas (il ne peut pas se connecter pour déplacer ou annuler).
 */
@Component
@RequiredArgsConstructor
public class EmailAppointmentChannel implements AppointmentNotificationChannel {

    public static final String NAME = "email";

    private final EmailSender emailSender;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void send(AppointmentNotice notice) {
        if (!notice.wantsEmail() || !StringUtils.hasText(notice.email())) {
            return;
        }
        EmailContent content = switch (notice.kind()) {
            case CONFIRMED -> {
                Cta cta = cta(notice);
                yield EmailTemplates.appointmentConfirmed(notice.fullName(), notice.dateInfo(), cta.label(), cta.url(), cta.note());
            }
            case REMINDER -> {
                Cta cta = cta(notice);
                yield EmailTemplates.appointmentReminder(notice.fullName(), notice.dateInfo(), cta.label(), cta.url(), cta.note());
            }
            case CANCELLED -> EmailTemplates.appointmentCancelled(notice.fullName(), notice.appUrl() + "/#rdv");
        };
        emailSender.send(new EmailMessage(notice.email(), content.subject(), content.text(), content.html()));
    }

    private record Cta(String label, String url, String note) {
    }

    private static Cta cta(AppointmentNotice notice) {
        if (!notice.guest()) {
            return new Cta("Voir mes rendez-vous", notice.appUrl() + "/client", null);
        }
        String number = StringUtils.hasText(notice.cabinetWhatsapp()) ? notice.cabinetWhatsapp() : notice.cabinetPhone();
        String digits = number == null ? "" : number.replaceAll("\\D", "");
        String note = StringUtils.hasText(notice.cabinetPhone())
                ? "Pour déplacer ou annuler, appelez le cabinet au " + notice.cabinetPhone() + " ou répondez avec le bouton ci-dessous."
                : "Pour déplacer ou annuler, répondez avec le bouton ci-dessous.";
        if (digits.length() < 8) {
            return new Cta("Écrire au cabinet", notice.appUrl() + "/contact", note);
        }
        String text = "Bonjour, c'est " + notice.fullName()
                + ". Je souhaite déplacer ou annuler mon rendez-vous" + notice.dateInfo() + ".";
        String url = "https://wa.me/" + digits + "?text=" + URLEncoder.encode(text, StandardCharsets.UTF_8);
        return new Cta("Déplacer ou annuler", url, note);
    }
}
