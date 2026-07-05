package com.example.forum.dto.response.chatResponseDto;

import com.example.forum.entity.Enum.ChatRole;
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
