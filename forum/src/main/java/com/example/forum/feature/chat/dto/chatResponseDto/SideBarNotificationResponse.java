package com.example.forum.feature.chat.dto.chatResponseDto;


import com.example.forum.domain.Enum.ChatEvent;
import com.example.forum.domain.Enum.MessageType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SideBarNotificationResponse {
    private Long chatId;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private MessageType lastMessageType;
    private String senderName;
    private Integer unreadCount;
    private ChatEvent eventType;
    private Boolean isMute;
}
