package com.example.forum.feature.chat.dto.chatRequestDto;

import lombok.Data;

@Data
public class UpdateChatSettingRequest {
    private Boolean isMuted;
    private Boolean isPinned;
    private Boolean isArchived;
}
