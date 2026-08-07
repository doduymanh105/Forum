package com.example.forum.feature.chat.dto.chatResponseDto;

import com.example.forum.domain.Enum.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private Long chatId;
    private String chatName;
    private String chatAvatarUrl;
    private boolean isGroup;
    private LocalDateTime createdAt;

    private boolean isPinned;
    private boolean isMuted;
    private boolean isArchived;
    private Long lastReadMessageId;
    private Long lastMessageId;
    private String lastSenderName;
    private String lastMessageContent;
    private MessageType lastMessageType;
    private LocalDateTime lastMessageAt;
    private int unreadCount;
}
