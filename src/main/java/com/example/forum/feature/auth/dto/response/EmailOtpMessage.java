package com.example.forum.feature.auth.dto.response;

import java.io.Serializable;

public record EmailOtpMessage(String email, String otpCode) implements Serializable {
}
