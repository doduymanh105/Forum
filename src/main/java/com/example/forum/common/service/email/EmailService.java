package com.example.forum.common.service.email;

public interface EmailService {
    void sendOtpMail(String toMail, String otpCode);

    void sendAlertNewDeviceLogin(String toEmail, String userAgent,String ipAddress, String loginTime);
}
