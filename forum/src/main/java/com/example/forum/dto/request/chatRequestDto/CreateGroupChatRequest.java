package com.example.forum.dto.request.chatRequestDto;

import lombok.Data;

import java.util.List;

@Data
public class CreateGroupChatRequest {
    private String groupName;
    private String groupAvatarUrl;
    private List<Long> userIdList;
}
