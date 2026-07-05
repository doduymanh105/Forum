package com.example.forum.dto.response.chatResponseDto;

import com.example.forum.entity.UserEntity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class UserResponse {
    private Long id;
    private String userName;
    private String displayName;
    private String avatarUrl;
    private LocalDateTime createdAt;

    public static UserResponse mapToUserResponse(UserEntity user){
        return UserResponse.builder()
                .id(user.getUserId())
                .userName(user.getUsername())
                .displayName(user.displayUsername())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
