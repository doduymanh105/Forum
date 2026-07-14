package com.example.forum.feature.chat.dto.chatRequestDto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class UserCreateRequest {
    @NotEmpty(message = "UserName can not be null")
    private String userName;

    @NotEmpty(message = "DisplayName can not be null")
    private String displayName;


    @NotEmpty(message = "invalid password")
    @Min(value = 8, message = "Password must be over 8 characters")
    private String password;
}
