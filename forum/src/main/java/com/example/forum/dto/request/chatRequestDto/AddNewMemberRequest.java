package com.example.forum.dto.request.chatRequestDto;

import lombok.Data;

import java.util.List;

@Data
public class AddNewMemberRequest {
    private List<Long> memberIdList;
}
