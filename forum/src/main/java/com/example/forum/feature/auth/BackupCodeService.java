package com.example.forum.feature.auth;

import com.example.forum.domain.UserEntity;

import java.util.List;

public interface BackupCodeService {
    List<String> generateBackupCode(UserEntity user);
    boolean verifyBackupCode(UserEntity user, String code);
}
