package com.example.forum.feature.chat.dto.chatResponseDto;

import com.example.forum.domain.Enum.ChatRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {
    private Long userId;
    private String fullName;
    private String avatarUrl;
    private ChatRole role;
    private Long lastReadMessageId;
    private LocalDateTime createdAt;
}
