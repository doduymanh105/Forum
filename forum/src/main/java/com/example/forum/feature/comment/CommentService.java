package com.example.forum.feature.comment;

import com.example.forum.feature.comment.dto.CreateCommentRequest;
import com.example.forum.feature.comment.dto.UpdateCommentRequest;
import com.example.forum.feature.comment.dto.CommentDto;
import com.example.forum.feature.comment.dto.CommentResponseDto;
import com.example.forum.common.dto.PagedResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface CommentService {
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    CommentResponseDto createComment(Long postId, CreateCommentRequest request);

    List<CommentDto> getListOfCommentByPath(Long postId,String path);

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    List<CommentDto> getListOfCommentAndCountReplyComment(Long postId);

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    CommentDto updateComment(Long commentId, UpdateCommentRequest request);

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    void softDeletedComment(Long commentId);

    @PreAuthorize("hasRole('ADMIN')")
    void hardDeletedComment(Long commentId);

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    List<CommentDto> getListOfCommentAndCountReplyComment(Long postId, String parentPath, Long parentId);

    PagedResponse<CommentResponseDto> getTopLevelComments(Long postId, Pageable pageable);

    List<CommentResponseDto> getReplies(Long parentId);
}
