package com.example.forum.feature.comment;

import com.example.forum.common.dto.ApiResponse;
import com.example.forum.core.annotation.RateLimit;
import com.example.forum.feature.comment.dto.CommentVoteResponse;
import com.example.forum.feature.comment.dto.VoteCommentRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Comment Vote API")
@RestController
@RequestMapping("/forum/post/comments")
@RequiredArgsConstructor
public class CommentVoteController {

    private final CommentVoteService commentVoteService;

    @RateLimit(capacity = 20, time = 1)
    @PostMapping("/{commentId}/vote")
    public ResponseEntity<ApiResponse<CommentVoteResponse>> voteComment(
            @PathVariable Long commentId,
            @RequestBody VoteCommentRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Vote processed successfully!",
                        commentVoteService.voteComment(commentId,request.getVoteType())
                )
        );
    }
}
