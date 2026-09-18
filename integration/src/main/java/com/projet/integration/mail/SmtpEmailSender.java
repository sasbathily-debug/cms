package com.projet.integration.mail;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.mail", name = "provider", havingValue = "smtp")
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender javaMailSender;
    private final String from;

    public SmtpEmailSender(JavaMailSender javaMailSender, @Value("${app.mail.from:}") String from) {
        this.javaMailSender = javaMailSender;
        this.from = from;
    }

    @Override
    public void send(EmailMessage message) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            if (StringUtils.hasText(from)) {
                helper.setFrom(from);
            }
            helper.setTo(message.to());
            helper.setSubject(message.subject());
            helper.setText(message.text(), message.html());
            javaMailSender.send(mimeMessage);
            log.info("Email envoyé à {}", message.to());
        } catch (Exception e) {
            throw new RuntimeException("Échec de l'envoi de l'email à " + message.to(), e);
        }
    }
}
