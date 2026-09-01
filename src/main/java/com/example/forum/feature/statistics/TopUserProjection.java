package com.example.forum.feature.statistics;

public interface TopUserProjection {
    Long getUserId();
    String getUserName();
    String getEmail();
    String getAvatarUrl();
    Long getTotalPosts();
}
