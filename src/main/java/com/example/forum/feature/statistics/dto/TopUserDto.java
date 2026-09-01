package com.example.forum.feature.statistics.dto;

import com.example.forum.domain.UserEntity;
import com.example.forum.feature.statistics.TopUserProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopUserDto {
    private Long userId;
    private String username;
    private String email;
    private String avatarUrl;
    private Long totalPosts;

    public static TopUserDto mapToTopUserDto(TopUserProjection userProjection){
        return TopUserDto.builder()
                .userId(userProjection.getUserId())
                .username(userProjection.getUserName())
                .email(userProjection.getEmail())
                .avatarUrl(userProjection.getAvatarUrl())
                .totalPosts(userProjection.getTotalPosts())
                .build();
    }
}
