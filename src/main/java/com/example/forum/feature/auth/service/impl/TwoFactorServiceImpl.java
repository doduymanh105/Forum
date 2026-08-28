package com.example.forum.feature.auth.service.impl;

import com.example.forum.common.constant.AppConstants;
import com.example.forum.common.service.cache.RedisService;
import com.example.forum.core.exception.AppException;
import com.example.forum.core.exception.ErrorCode;
import com.example.forum.domain.UserEntity;
import com.example.forum.feature.auth.dto.response.TwoFactorResponse;
import com.example.forum.feature.auth.repository.BackupCodeRepository;
import com.example.forum.feature.auth.service.BackupCodeService;
import com.example.forum.feature.auth.service.TwoFactorService;
import com.example.forum.feature.user.UserRepository;
import com.example.forum.common.service.cache.CacheService;
import com.example.forum.common.utils.SecurityUtils;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class TwoFactorServiceImpl implements TwoFactorService {
    private final GoogleAuthenticator googleAuthenticator;
    private final SecurityUtils securityService;
    private final CacheService redisService;
    private final BackupCodeService backupCodeService;

    private final BackupCodeRepository backupCodeRepository;
    private final UserRepository userRepository;

    @Value("${app.2fa.secret.timeout}")
    private long setup2faTimeout;

    public TwoFactorServiceImpl(UserRepository userRepository,
                                RedisService redisService,
                                BackupCodeServiceImpl backupCodeService,
                                BackupCodeRepository backupCodeRepository,
                                SecurityUtils securityService){
        GoogleAuthenticatorConfig config = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                .setTimeStepSizeInMillis(30000)
                .setWindowSize(1)
                .setCodeDigits(6)
                .build();
        this.googleAuthenticator = new GoogleAuthenticator(config);
        this.userRepository= userRepository;
        this.redisService=redisService;
        this.backupCodeService=backupCodeService;
        this.backupCodeRepository=backupCodeRepository;
        this.securityService=securityService;
    }

    @Override
    public String generateNewSecret(){
        return googleAuthenticator.createCredentials().getKey();
    }

    @Override
    public String generateQrCodeUri(String secret, String email){
        return String.format("otpauth://totp/MyForum:%s?secret=%s&issuer=MyForum", email, secret);
    }

    @Override
    public boolean isOtpValid(String secret, int code) {
        return googleAuthenticator.authorize(secret, code);
    }

    @Override
    public TwoFactorResponse enableTwoFactor(String email){
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));

        String secret = generateNewSecret();
        String qrUrl = generateQrCodeUri(secret, email);
        redisService.set(AppConstants.PREFIX_TEMP_2FA+email, secret,setup2faTimeout, TimeUnit.SECONDS);

        return TwoFactorResponse.builder()
                .secret(secret)
                .qrUrl(qrUrl)
                .build();
    }

    @Override
    @Transactional
    public List<String> verifyOtp(String email, int otp){
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));

        String keyTemp2fa = AppConstants.PREFIX_TEMP_2FA + email;

        Object storedSecret = redisService.get(keyTemp2fa);
        if (storedSecret == null) {
            throw new AppException(ErrorCode.CODE_2FA_EXPIRED);
        }

        String secretStr = storedSecret.toString();
        boolean result = isOtpValid(secretStr,otp);
        if (!result) {
            throw new AppException(ErrorCode.OTP_INVALID);
        }
        if(!user.isTwoFactorEnabled()){
            user.setTwoFactorEnabled(true);
            user.setTwoFactorSecret(secretStr);
            userRepository.save(user);
        }

        List<String> backupCodes = backupCodeService.generateBackupCode(user);
        redisService.delete(keyTemp2fa);

        return backupCodes;

    }

    @Transactional
    @Override
    public void disable2fa(UserEntity user, String password){

        securityService.validatePassword(user, password);

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        userRepository.save(user);
        backupCodeRepository.deleteByUserEntityUserId(user.getUserId());
    }

    @Override
    public boolean is2faEnable() {
        UserEntity user = securityService.getCurrentUser();
        return user.isTwoFactorEnabled();
    }
}
