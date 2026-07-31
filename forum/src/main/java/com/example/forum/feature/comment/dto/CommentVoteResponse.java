package com.example.forum.feature.comment.dto;

import com.example.forum.domain.Enum.VoteType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CommentVoteResponse {
    private Long commentId;
    private Long upvotes;
    private Long downvotes;
    private Long score;
    private VoteType userVote;
}
