package com.example.forum.feature.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentContextResponse {
    private Long postId;
    private Long highlightCommentId;
    private List<CommentDto> threadContext;
}
