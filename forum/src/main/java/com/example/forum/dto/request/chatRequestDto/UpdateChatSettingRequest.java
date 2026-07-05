package com.example.forum.dto.request.chatRequestDto;

import lombok.Data;

@Data
public class UpdateChatSettingRequest {
    private Boolean isMuted;
    private Boolean isPinned;
    private Boolean isArchived;
}
