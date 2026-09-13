package com.example.forum.feature.auth.dto.response;

import java.io.Serializable;

public record DeviceLoginMessage(String email,
                                 String userAgent,
                                 String ipAddress,
                                 String loginTime)
        implements Serializable {
}
