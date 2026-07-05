package com.example.forum.dto.request.chatRequestDto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MessageRequest {
    @NotBlank
    private String content;

    private Long replyId;
}
