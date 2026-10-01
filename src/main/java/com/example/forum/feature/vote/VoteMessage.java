package com.example.forum.feature.vote;

import java.io.Serializable;

public record VoteMessage(
        Long userId,
        Long postId,
        Long postAuthorId,
        String senderName,
        String voteType
) implements Serializable {}
