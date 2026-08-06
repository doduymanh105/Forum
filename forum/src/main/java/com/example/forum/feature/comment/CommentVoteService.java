package com.example.forum.feature.comment;

import com.example.forum.domain.Enum.VoteType;
import com.example.forum.feature.comment.dto.CommentVoteResponse;

public interface CommentVoteService {
    CommentVoteResponse voteComment(Long commentId, VoteType requestVoteType);
}
