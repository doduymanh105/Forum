package com.example.forum.feature.auth.worker;

import com.example.forum.common.service.email.EmailService;
import com.example.forum.core.config.RabbitMqConfig;
import com.example.forum.feature.auth.dto.response.DeviceLoginMessage;
import com.example.forum.feature.auth.dto.response.EmailOtpMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationWorker {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMqConfig.EMAIL_QUEUE)
    public void processEmailOpt(EmailOtpMessage message){
        log.info("[WORKER] Process sending OTP to email: {}", message.email());

        try{
            emailService.sendOtpMail(message.email(), message.otpCode());
        } catch (Exception e){
            log.error("[Worker] Fail to send email to {}", message.email());
            throw e;
        }
    }

    @RabbitListener(queues = RabbitMqConfig.ALERT_QUEUE)
    public void processLoginAlert(DeviceLoginMessage message){
        log.info("[WORKER] Process sending login alert to email: {}", message.email());

        try {
            emailService.sendAlertNewDeviceLogin(
                    message.email(),
                    message.userAgent(),
                    message.ipAddress(),
                    message.loginTime()
            );
        } catch (Exception e) {
            log.error("[Worker] Fail to send alert email to {}", message.email(), e);
            throw e;
        }
    }
}
