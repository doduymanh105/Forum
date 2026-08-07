package com.example.forum.feature.vote;

import com.example.forum.domain.Enum.VoteType;

public interface VoteProjection {
    Long getUserId();
    String getUsername();
    String getAvatarUrl();
    VoteType getVoteType();
}
