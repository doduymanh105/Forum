package com.example.forum.feature.auth;

import com.example.forum.domain.UserEntity;

public interface DeviceService {
    boolean saveUserDevice(UserEntity user, String deviceId, String rawRefreshToken, String ua, String ip);
}
