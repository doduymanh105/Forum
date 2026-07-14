package com.example.forum.feature.auth;

import com.example.forum.feature.auth.dto.request.AuthenticationRequest;
import com.example.forum.feature.auth.dto.request.LogoutRequest;
import com.example.forum.feature.auth.dto.request.RegisterRequest;
import com.example.forum.feature.auth.dto.request.ResetPasswordRequest;
import com.example.forum.feature.auth.dto.response.AuthenticationResponse;
import com.example.forum.feature.auth.dto.response.UserDeviceResponse;
import com.example.forum.feature.user.dto.UserSummaryDto;
import com.example.forum.feature.auth.dto.response.VerifyOtpResponse;
import com.example.forum.domain.UserEntity;

import java.util.List;

public interface AuthenticationService {

    UserSummaryDto register(RegisterRequest request);
    VerifyOtpResponse verifyCode(String email, String code);

    AuthenticationResponse authenticate(AuthenticationRequest request);

    AuthenticationResponse verifyTwoFactorLogin(String email, String otpCode, String deviceId);

    AuthenticationResponse finalizeLogin(UserEntity user, String deviceId);

    boolean saveUserDevice(UserEntity user, String deviceId, String rawRefreshToken, String ua, String ip);

    AuthenticationResponse refreshToken(String rawRefreshToken);

    void logout(LogoutRequest request, String accessToken);

    void forgotPassword(String email);

    void resetPassword(ResetPasswordRequest request);

    List<UserDeviceResponse> getAllDevice(UserEntity user, String currentUserDeviceId);

    void revokeDevice(UserEntity user, String deviceTargetId);

    void revokeAllDevice(UserEntity currentUser);

}
