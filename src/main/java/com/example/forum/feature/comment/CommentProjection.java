package com.example.forum.feature.comment;

import java.time.LocalDateTime;

public interface CommentProjection {
    Long getCommentId();
    String getCommentContent();
    String getCommentPath();
    Long getParentId();
    Boolean getIsDeleted();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();

    Long getUserId();
    String getUsername();
    String getEmail();
    String getAvatarUrl();

    Long getPostId();

    Long getUpvotes();
    Long getDownvotes();
    Long getScore();
    String getUserVote();

    Long getReplyCount();

    String getReplyToUsername();
}

