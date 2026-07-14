package com.example.forum.feature.chat.dto.chatRequestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EditMessageRequest {

    @NotBlank
    private String content;
}
