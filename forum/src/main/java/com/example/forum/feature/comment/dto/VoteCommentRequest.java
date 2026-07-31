package com.example.forum.feature.comment.dto;

import com.example.forum.domain.Enum.VoteType;
import lombok.Data;

@Data
public class VoteCommentRequest {
    private VoteType voteType;
}
