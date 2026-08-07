package com.example.forum.feature.chat.dto.chatResponseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatOverview {
    private Long id;
    private String chatName;
    private String chatAvatarUrl;
    private boolean isGroup;
    private LocalDateTime createdAt;
}
