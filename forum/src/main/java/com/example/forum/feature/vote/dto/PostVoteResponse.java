package com.example.forum.feature.vote.dto;

import com.example.forum.domain.Enum.VoteType;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class PostVoteResponse {
    private Long postId;
    private Long upvotes;
    private Long downvotes;
    private Long score;
    private VoteType userVote;
}
