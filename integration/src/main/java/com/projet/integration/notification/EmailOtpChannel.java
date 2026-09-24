package com.projet.integration.notification;

import com.projet.integration.mail.EmailContent;
import com.projet.integration.mail.EmailMessage;
import com.projet.integration.mail.EmailSender;
import com.projet.integration.mail.EmailTemplates;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.otp", name = "channel", havingValue = "email", matchIfMissing = true)
public class EmailOtpChannel implements OtpChannel {

    private final EmailSender emailSender;

    @Override
    public void sendOtp(String email, String phone, String firstName, String code, int ttlMinutes) {
        EmailContent content = EmailTemplates.otpVerification(firstName, code, ttlMinutes);
        emailSender.send(new EmailMessage(email, content.subject(), content.text(), content.html()));
    }
}
