package com.example.forum.feature.chat.dto.chatRequestDto;

import lombok.Data;

import java.util.List;

@Data
public class AddNewMemberRequest {
    private List<Long> memberIdList;
}
