package com.example.forum.feature.auth.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResetPasswordRequest {
    private String email;
    private String resetToken;
    private String newPassword;
}
