package com.example.forum.feature.auth.service;

import com.example.forum.domain.UserEntity;
import com.example.forum.feature.auth.dto.response.VerifyOtpResponse;

public interface VerificationService {
    void sendVerificationEmail(UserEntity user);

    VerifyOtpResponse verifyToken(String email, String inputToken);

    void resendVerificationCode(String email);
}
