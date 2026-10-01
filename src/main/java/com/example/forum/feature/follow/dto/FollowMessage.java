package com.example.forum.feature.follow.dto;

import java.io.Serializable;

public record FollowMessage(
        Long followerId,
        Long followingId,
        String creatorName,
        String action
) implements Serializable {
}
