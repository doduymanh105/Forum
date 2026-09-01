package com.example.forum.common.service.email;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mail.service.type", havingValue = "resend")
public class ResendEmailServiceImpl implements EmailService{

    @Value("${resend.api.key}")
    private String apiKey;

    private final TemplateEngine templateEngine;

    @Async
    @Override
    public void sendOtpMail(String toMail, String otpCode) {

        Context context = new Context();
        context.setVariable("otpCode", otpCode);

        String htmlContent = templateEngine.process("email-otp", context);

        Resend resend = new Resend(apiKey);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("Forum System <noreply@duymanhdo.id.vn>")
                .to(toMail)
                .subject("Verify Email for Forum")
                .html(htmlContent)
                .build();

        try {
            resend.emails().send(params);
        } catch (ResendException e) {
            log.error(e.getMessage());
            throw new RuntimeException("Error: Resend fail to mail: " + e.getMessage());
        }
    }

    @Async
    @Override
    public void sendAlertNewDeviceLogin(String toEmail, String userAgent, String ipAddress, String loginTime) {
        Resend resend = new Resend(apiKey);
        Context context = new Context();
        context.setVariable("deviceName", userAgent);
        context.setVariable("ipAddress", ipAddress);
        context.setVariable("loginTime", loginTime);

        String htmlContent = templateEngine.process("login-alert", context);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("Forum System <noreply@duymanhdo.id.vn>")
                .to(toEmail)
                .subject("Forum login alert!")
                .html(htmlContent)
                .build();

        try {
            resend.emails().send(params);
        } catch (ResendException e) {
            log.error(e.getMessage());
            throw new RuntimeException("Error: Resend fail to mail: " + e.getMessage());
        }
    }
}
