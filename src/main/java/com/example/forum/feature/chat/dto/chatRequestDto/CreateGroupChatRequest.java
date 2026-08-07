package com.example.forum.feature.chat.dto.chatRequestDto;

import lombok.Data;

import java.util.List;

@Data
public class CreateGroupChatRequest {
    private String groupName;
    private String groupAvatarUrl;
    private List<Long> userIdList;
}
