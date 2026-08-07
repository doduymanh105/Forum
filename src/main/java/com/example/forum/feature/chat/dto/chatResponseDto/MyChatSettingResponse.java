package com.example.forum.feature.chat.dto.chatResponseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyChatSettingResponse {
    private boolean isMuted;
    private boolean isPinned;
    private boolean isArchived;
    private int unreadCount;
}
