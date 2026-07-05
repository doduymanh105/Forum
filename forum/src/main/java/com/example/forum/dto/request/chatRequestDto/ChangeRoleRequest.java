package com.example.forum.dto.request.chatRequestDto;

import com.example.forum.entity.Enum.ChatRole;
import lombok.Data;

@Data
public class ChangeRoleRequest {
    private ChatRole role;
}
