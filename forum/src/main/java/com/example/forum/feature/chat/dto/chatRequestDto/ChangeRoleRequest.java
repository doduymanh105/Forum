package com.example.forum.feature.chat.dto.chatRequestDto;

import com.example.forum.domain.Enum.ChatRole;
import lombok.Data;

@Data
public class ChangeRoleRequest {
    private ChatRole role;
}
