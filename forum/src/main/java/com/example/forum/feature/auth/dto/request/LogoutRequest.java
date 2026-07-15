package com.example.forum.feature.auth.dto.request;

import lombok.Data;

@Data
public class LogoutRequest {
    private String refreshToken;
}
