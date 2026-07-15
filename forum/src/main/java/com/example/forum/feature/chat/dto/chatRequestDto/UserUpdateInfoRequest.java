package com.example.forum.feature.chat.dto.chatRequestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateInfoRequest {

    @NotBlank(message = "displayName can not be empty")
    @Size(min = 5, message = "DisplayName must have at least 5 characters")
    private String displayName;
}
