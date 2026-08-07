package com.example.forum.feature.comment;


import com.example.forum.feature.comment.dto.CreateCommentRequest;
import com.example.forum.feature.comment.dto.UpdateCommentRequest;
import com.example.forum.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/forum/post/comment")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/create")
    ResponseEntity<?> createComment(
            @RequestParam Long postId,
            @RequestBody CreateCommentRequest request
            ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body( ApiResponse.created(
                        "Comment created",
                        commentService.createComment(postId,request)
                ));
    }
    @GetMapping("/{postId}/rootCommentWithCount")
    ResponseEntity<?> getRootCommentWithReplyCount(
            @PathVariable Long postId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get root comments successfully",
                        commentService.getListOfRootCommentAndCountReplyComment(postId, cursor ,sortBy, size)
        ));
    }
    // for specific comment
    @GetMapping("/{postId}/{parentId}/replies")
    ResponseEntity<?> getCommentWithReplyCount(
            @PathVariable Long postId,
            @PathVariable Long parentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size

    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get child comments successfully",
                        commentService.getListOfChildCommentAndCountReplyComment(postId,parentId, page, size)
                )
        );
    }

    @PatchMapping("/{commentId}/update")
    ResponseEntity<?> updateComment (
            @PathVariable Long commentId,
            @RequestBody UpdateCommentRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Comment updated!",
                        commentService.updateComment(commentId, request)
        ));
    }

    @PatchMapping("/{commentId}")
    ResponseEntity<?> softDeletedComment(
            @PathVariable Long commentId
    ) {
        commentService.softDeletedComment(commentId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Comment deleted!",
                        null
                )
        );

    }

    @DeleteMapping("/{commentId}")
    ResponseEntity<?> hardDeletedComment(
            @PathVariable Long commentId
    ){
        commentService.hardDeletedComment(commentId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Permanently deleted comment",
                        null
                )
        );
    }

    @GetMapping("/{commentId}/context")
    public ResponseEntity<?> getCommentContext(
            @PathVariable("commentId") Long commentId
    ) {
        return ResponseEntity.ok(
                ApiResponse
                        .success("Comment's context get",
                                commentService.getCommentContext(commentId))
        );
    }


}
