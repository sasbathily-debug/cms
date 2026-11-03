package com.projet.integration.mail;

/**
 * Construit les emails transactionnels (texte + HTML) avec l'identité visuelle du
 * cabinet. Le HTML est en tables + styles inline pour rester lisible dans le plus
 * de clients mail possible (Gmail, Outlook, Apple Mail...).
 */
public final class EmailTemplates {

    private static final String FOREST = "#142B22";
    private static final String CREAM = "#FBF6EC";
    private static final String INK_600 = "#5B6459";
    private static final String BORDER = "#E6DCC4";
    private static final String PRACTICE_NAME = "Dr Aïssatou Mamadou Fall";
    private static final String PRACTICE_TAGLINE = "Endocrinologie &amp; Nutrition &middot; Dakar";

    private EmailTemplates() {
    }

    public static EmailContent otpVerification(String firstName, String code, int ttlMinutes) {
        String subject = "Votre code de vérification";
        String text = "Bonjour " + firstName + ",\n\nVotre code de vérification est : " + code
                + " (valide " + ttlMinutes + " minutes).\n\nCabinet du Dr Aïssatou Mamadou Fall";

        String body = paragraph("Bonjour " + escape(firstName) + ",")
                + paragraph("Voici votre code de vérification pour activer votre compte :")
                + codeBox(code)
                + paragraph("Ce code est valable " + ttlMinutes + " minutes.");

        return new EmailContent(subject, text, wrap(subject, body, null, null));
    }

    public static EmailContent passwordReset(String firstName, String code, int ttlMinutes) {
        String subject = "Réinitialisation de votre mot de passe";
        String text = "Bonjour " + firstName + ",\n\nVotre code pour choisir un nouveau mot de passe est : " + code
                + " (valide " + ttlMinutes + " minutes).\n\nSi vous n'êtes pas à l'origine de cette demande, ignorez ce message : "
                + "votre mot de passe actuel reste inchangé.\n\nCabinet du Dr Aïssatou Mamadou Fall";

        String body = paragraph("Bonjour " + escape(firstName) + ",")
                + paragraph("Voici votre code pour choisir un nouveau mot de passe :")
                + codeBox(code)
                + paragraph("Ce code est valable " + ttlMinutes + " minutes.")
                + paragraph("Si vous n'êtes pas à l'origine de cette demande, ignorez ce message : votre mot de passe actuel reste inchangé.");

        return new EmailContent(subject, text, wrap(subject, body, null, null));
    }

    public static EmailContent bookingCode(String fullName, String code, int ttlMinutes) {
        String subject = "Votre code pour confirmer votre rendez-vous";
        String text = "Bonjour " + fullName + ",\n\nVotre code pour confirmer votre demande de rendez-vous est : " + code
                + " (valide " + ttlMinutes + " minutes).\n\nSi vous n'êtes pas à l'origine de cette demande, ignorez ce message : "
                + "aucun rendez-vous ne sera pris.\n\nCabinet du Dr Aïssatou Mamadou Fall";

        String body = paragraph("Bonjour " + escape(fullName) + ",")
                + paragraph("Voici votre code pour confirmer votre rendez-vous :")
                + codeBox(code)
                + paragraph("Ce code est valable " + ttlMinutes + " minutes.")
                + paragraph("Si vous n'êtes pas à l'origine de cette demande, ignorez ce message : aucun rendez-vous ne sera pris.");

        return new EmailContent(subject, text, wrap(subject, body, null, null));
    }

    public static EmailContent appointmentConfirmed(String fullName, String dateInfo, String appUrl) {
        return appointmentConfirmed(fullName, dateInfo, "Voir mes rendez-vous", appUrl, null);
    }

    /** `note` (facultative) : une phrase en plus, par exemple comment déplacer ou annuler sans compte. */
    public static EmailContent appointmentConfirmed(String fullName, String dateInfo, String ctaLabel, String ctaUrl, String note) {
        String subject = "Votre rendez-vous est confirmé";
        String text = "Bonjour " + fullName + ",\n\nVotre rendez-vous a été confirmé" + dateInfo
                + "." + (note != null ? "\n\n" + note : "")
                + "\n\nÀ bientôt,\nCabinet du Dr Aïssatou Mamadou Fall";

        String body = paragraph("Bonjour " + escape(fullName) + ",")
                + statusBadge("Confirmé", "#E3F1E6", "#2E7D46")
                + paragraph("Votre rendez-vous a été confirmé" + escape(dateInfo) + ".")
                + (note != null ? paragraph(escape(note)) : "")
                + paragraph("À bientôt !");

        return new EmailContent(subject, text, wrap(subject, body, ctaLabel, ctaUrl));
    }

    public static EmailContent appointmentReminder(String fullName, String dateInfo, String appUrl) {
        return appointmentReminder(fullName, dateInfo, "Voir mes rendez-vous", appUrl, null);
    }

    public static EmailContent appointmentReminder(String fullName, String dateInfo, String ctaLabel, String ctaUrl, String note) {
        String subject = "Rappel : votre rendez-vous a lieu demain";
        String text = "Bonjour " + fullName + ",\n\nPetit rappel : votre rendez-vous a lieu demain" + dateInfo
                + "." + (note != null ? "\n\n" + note : "")
                + "\n\nÀ demain,\nCabinet du Dr Aïssatou Mamadou Fall";

        String body = paragraph("Bonjour " + escape(fullName) + ",")
                + statusBadge("Rappel", "#FDF0DC", "#B5762A")
                + paragraph("Petit rappel : votre rendez-vous a lieu demain" + escape(dateInfo) + ".")
                + (note != null ? paragraph(escape(note)) : "")
                + paragraph("À demain !");

        return new EmailContent(subject, text, wrap(subject, body, ctaLabel, ctaUrl));
    }

    public static EmailContent appointmentCancelled(String fullName, String appUrl) {
        String subject = "Votre rendez-vous a été annulé";
        String text = "Bonjour " + fullName
                + ",\n\nVotre demande de rendez-vous a été annulée. N'hésitez pas à nous recontacter pour reprogrammer."
                + "\n\nCabinet du Dr Aïssatou Mamadou Fall";

        String body = paragraph("Bonjour " + escape(fullName) + ",")
                + statusBadge("Annulé", "#FBE7E6", "#B2453B")
                + paragraph("Votre demande de rendez-vous a été annulée. N'hésitez pas à nous recontacter pour reprogrammer.");

        return new EmailContent(subject, text, wrap(subject, body, "Reprendre rendez-vous", appUrl));
    }

    public static EmailContent contactMessage(String name, String phone, String email, String motif, String message) {
        String subject = "Contact du site : " + motif + " (" + name + ")";
        String text = "Nouveau message depuis le formulaire de contact du site.\n\n"
                + "Nom : " + name + "\n"
                + "Téléphone : " + phone + "\n"
                + "E-mail : " + (email == null || email.isBlank() ? "-" : email) + "\n"
                + "Motif : " + motif + "\n\n"
                + message;

        String body = paragraph("Nouveau message depuis le formulaire de contact du site.")
                + paragraph("<strong>Nom :</strong> " + escape(name) + "<br>"
                + "<strong>Téléphone :</strong> " + escape(phone) + "<br>"
                + "<strong>E-mail :</strong> " + escape(email == null || email.isBlank() ? "-" : email) + "<br>"
                + "<strong>Motif :</strong> " + escape(motif))
                + paragraph(escape(message).replace("\n", "<br>"));

        return new EmailContent(subject, text, wrap(subject, body, null, null));
    }

    private static String paragraph(String text) {
        return "<p style=\"margin:0 0 16px;font-size:15px;line-height:1.6;color:" + INK_600 + ";\">" + text + "</p>";
    }

    private static String codeBox(String code) {
        return "<div style=\"margin:0 0 20px;padding:18px;border:1px solid " + BORDER + ";border-radius:12px;"
                + "background:" + CREAM + ";text-align:center;\">"
                + "<span style=\"font-family:'Courier New',monospace;font-size:28px;font-weight:700;letter-spacing:6px;color:"
                + FOREST + ";\">" + escape(code) + "</span></div>";
    }

    private static String statusBadge(String label, String bg, String fg) {
        return "<div style=\"margin:0 0 16px;\">"
                + "<span style=\"display:inline-block;padding:6px 14px;border-radius:999px;background:" + bg + ";"
                + "color:" + fg + ";font-size:13px;font-weight:600;\">" + escape(label) + "</span></div>";
    }

    private static String wrap(String title, String bodyHtml, String ctaLabel, String ctaUrl) {
        String cta = "";
        if (ctaLabel != null && ctaUrl != null) {
            cta = "<div style=\"margin-top:8px;\">"
                    + "<a href=\"" + ctaUrl + "\" style=\"display:inline-block;padding:13px 28px;border-radius:999px;"
                    + "background:" + FOREST + ";color:#ffffff;text-decoration:none;font-family:Arial,Helvetica,sans-serif;"
                    + "font-size:14px;font-weight:600;\">" + escape(ctaLabel) + "</a></div>";
        }

        return "<!doctype html><html><body style=\"margin:0;padding:0;background:#F3E9D6;"
                + "font-family:Arial,Helvetica,sans-serif;\">"
                + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#F3E9D6;padding:32px 16px;\">"
                + "<tr><td align=\"center\">"
                + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"max-width:520px;background:#ffffff;border-radius:16px;overflow:hidden;border:1px solid "
                + BORDER + ";\">"
                + "<tr><td style=\"padding:28px 32px;border-bottom:1px solid " + BORDER + ";\">"
                + "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\"><tr>"
                + "<td style=\"width:44px;height:44px;border-radius:50%;background:" + FOREST
                + ";text-align:center;vertical-align:middle;\">"
                + "<span style=\"color:#ffffff;font-size:14px;font-weight:700;\">AF</span></td>"
                + "<td style=\"padding-left:12px;\">"
                + "<div style=\"font-size:15px;font-weight:700;color:" + FOREST + ";\">" + PRACTICE_NAME + "</div>"
                + "<div style=\"font-size:12px;color:" + INK_600 + ";\">" + PRACTICE_TAGLINE + "</div>"
                + "</td></tr></table></td></tr>"
                + "<tr><td style=\"padding:32px;\">"
                + "<h1 style=\"margin:0 0 20px;font-size:20px;font-weight:700;color:" + FOREST + ";\">" + escape(title) + "</h1>"
                + bodyHtml
                + cta
                + "</td></tr>"
                + "<tr><td style=\"padding:20px 32px;background:" + CREAM + ";border-top:1px solid " + BORDER + ";\">"
                + "<div style=\"font-size:12px;color:" + INK_600 + ";\">" + PRACTICE_NAME + " &mdash; Sacré-Cœur, Dakar</div>"
                + "</td></tr>"
                + "</table></td></tr></table></body></html>";
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
