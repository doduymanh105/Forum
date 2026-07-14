package com.example.forum.feature.chat.dto.chatResponseDto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ReadReceiptResponse {
    private Long chatId;
    private Long userId;
    private Long lastReadMessageId;
    private int unreadCount;
}
