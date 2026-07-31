package com.example.forum.feature.comment;

import com.example.forum.common.dto.ApiResponse;
import com.example.forum.feature.comment.dto.VoteCommentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/forum/post/comments")
@RequiredArgsConstructor
public class CommentVoteController {

    private final CommentVoteService commentVoteService;

    @PostMapping("/{commentId}/vote")
    public ResponseEntity<?> voteComment(
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
