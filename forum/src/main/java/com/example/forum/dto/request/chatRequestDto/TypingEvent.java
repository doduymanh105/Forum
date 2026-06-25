package com.example.forum.dto.request.chatRequestDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingEvent {
    private Long chatId;
    private Long userId;
    private String senderName;
    private boolean isTyping;
}
