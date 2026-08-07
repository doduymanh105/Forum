package com.example.forum.feature.auth.service;

import com.example.forum.domain.UserEntity;
import com.example.forum.feature.auth.dto.response.TwoFactorResponse;

import java.util.List;

public interface TwoFactorService {
    String generateNewSecret();

    String generateQrCodeUri(String secret, String email);

    boolean isOtpValid(String secret, int code) ;

    TwoFactorResponse enableTwoFactor(String email);

    List<String> verifyOtp(String email, int otp);

    void disable2fa(UserEntity user, String password);

    boolean is2faEnable();
}
