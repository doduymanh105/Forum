package com.example.forum.feature.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserUpdateRequest {
    private String username;
    private String avatarUrl;
}
