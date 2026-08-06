package com.example.forum.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comments")
@Builder
public class CommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @ManyToOne
    @JoinColumn(name = "post_id", referencedColumnName = "post_id")
    private PostEntity postEntity;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private UserEntity userEntity;

    private Long parentId;

//    if not this annotation , builder will assign null value
    @Builder.Default
    @Column(name = "upvotes", nullable = false, columnDefinition = "bigint default 0")
    private Long upvotes = 0L;

    @Builder.Default
    @Column(name = "downvotes", nullable = false, columnDefinition = "bigint default 0")
    private Long downvotes = 0L;

    @Builder.Default
    @Column(name = "score", nullable = false, columnDefinition = "bigint default 0")
    private Long score = 0L;

    @Column(name = "comment_content", columnDefinition = "TEXT")
    private String commentContent;

    @Column(name="comment_path")
    private String commentPath;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isDeleted = this.isDeleted != null ? this.isDeleted : false;
    }
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
