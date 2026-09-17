package com.projet.integration.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
        SimpleMailMessage mail = new SimpleMailMessage();
        if (StringUtils.hasText(from)) {
            mail.setFrom(from);
        }
        mail.setTo(message.to());
        mail.setSubject(message.subject());
        mail.setText(message.body());
        javaMailSender.send(mail);
        log.info("Email envoyé à {}", message.to());
    }
}
