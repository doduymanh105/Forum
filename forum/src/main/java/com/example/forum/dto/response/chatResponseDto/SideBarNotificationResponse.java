package com.example.forum.dto.response.chatResponseDto;


import com.example.forum.entity.Enum.ChatEvent;
import com.example.forum.entity.Enum.MessageType;
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
