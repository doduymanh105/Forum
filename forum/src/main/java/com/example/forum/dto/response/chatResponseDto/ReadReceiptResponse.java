package com.example.forum.dto.response.chatResponseDto;

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
