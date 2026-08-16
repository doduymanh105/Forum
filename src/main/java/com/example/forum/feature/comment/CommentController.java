package com.example.forum.feature.comment;


import com.example.forum.common.dto.CursorResponse;
import com.example.forum.common.dto.PagedResponse;
import com.example.forum.core.annotation.RateLimit;
import com.example.forum.feature.comment.dto.CommentContextResponse;
import com.example.forum.feature.comment.dto.CommentDto;
import com.example.forum.feature.comment.dto.CreateCommentRequest;
import com.example.forum.feature.comment.dto.UpdateCommentRequest;
import com.example.forum.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Comment API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/forum/post/comment")
public class CommentController {

    private final CommentService commentService;

    @RateLimit(capacity = 10, time = 1)
    @PostMapping("/create")
    ResponseEntity<ApiResponse<CommentDto>> createComment(
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

    @RateLimit(capacity = 100, time = 1)
    @GetMapping("/{postId}/rootCommentWithCount")
    ResponseEntity<ApiResponse<CursorResponse<CommentDto>>> getRootCommentWithReplyCount(
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
    @RateLimit(capacity = 100, time = 1)
    @GetMapping("/{postId}/{parentId}/replies")
    ResponseEntity<ApiResponse<PagedResponse<CommentDto>>> getCommentWithReplyCount(
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

    @RateLimit(capacity = 20, time = 1)
    @PatchMapping("/{commentId}/update")
    ResponseEntity<ApiResponse<CommentDto>> updateComment (
            @PathVariable Long commentId,
            @RequestBody UpdateCommentRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Comment updated!",
                        commentService.updateComment(commentId, request)
        ));
    }

    @RateLimit(capacity = 20, time = 1)
    @PatchMapping("/{commentId}")
    ResponseEntity<ApiResponse<?>> softDeletedComment(
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

    @RateLimit(capacity = 20, time = 1)
    @DeleteMapping("/{commentId}")
    ResponseEntity<ApiResponse<?>> hardDeletedComment(
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

    @RateLimit(capacity = 100, time = 1)
    @GetMapping("/{commentId}/context")
    public ResponseEntity<ApiResponse<CommentContextResponse>> getCommentContext(
            @PathVariable("commentId") Long commentId
    ) {
        return ResponseEntity.ok(
                ApiResponse
                        .success("Comment's context get",
                                commentService.getCommentContext(commentId))
        );
    }


}
