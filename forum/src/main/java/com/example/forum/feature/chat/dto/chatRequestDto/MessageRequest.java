package com.example.forum.feature.chat.dto.chatRequestDto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MessageRequest {
    @NotBlank
    private String content;

    private Long replyId;
}
