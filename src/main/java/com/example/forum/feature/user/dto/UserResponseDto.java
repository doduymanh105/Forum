package com.example.forum.feature.user.dto;


import com.example.forum.domain.Enum.SocialPlatform;
import com.example.forum.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
    private Long userId;
    private String email;
    private String userName;
    private String avatarUrl;
    private String bio;
    private Map<SocialPlatform, String> socialPlatforms;
    private Set<Role> roles;
    private Boolean isVerified;
    private LocalDateTime createdAt;
    private Long followerCount;
    private Long followingCount;
    private Boolean isFollowing;
}
