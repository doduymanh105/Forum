package com.example.forum.feature.chat.dto.chatRequestDto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

@Getter
public class LoginRequest {

    @NotEmpty
    private String username;
    @NotEmpty
    private String password;
}
