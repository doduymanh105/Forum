package com.example.forum.feature.comment;

import com.example.forum.domain.CommentEntity;
import com.example.forum.domain.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    @Query(value = """
        SELECT
           c.comment_id AS commentId,
           c.comment_content AS commentContent,
           c.comment_path AS commentPath,
           c.parent_id as parentId,
           c.is_deleted AS isDeleted,
           c.created_at AS createdAt,
           c.updated_at AS updatedAt,
           c.post_id AS postId,
           u.user_id AS userId,
           u.user_name AS username,
           u.email as email,
           u.avatar_url AS avatarUrl,
           c.upvotes AS upvotes,
           c.downvotes AS downvotes,
           c.score AS score,
           cv.vote_type AS userVote,
           COUNT(DISTINCT r.comment_id) AS replyCount
       FROM comments c
       JOIN users u ON c.user_id = u.user_id
       LEFT JOIN comments r
              ON r.post_id = c.post_id
             AND r.comment_path LIKE CONCAT(c.comment_path, c.comment_id, '/%')
             AND r.comment_id <> c.comment_id
       LEFT JOIN comment_votes cv
             ON cv.comment_id = c.comment_id
             AND cv.user_id = :currentUserId
       WHERE c.post_id = :postId
         AND c.is_deleted = false
         AND (c.parent_id IS NULL OR c.comment_path = '/')
         AND (:cursor IS NULL OR c.comment_id < :cursor)
       GROUP BY c.comment_id, u.user_id, u.user_name, u.email, u.avatar_url, cv.vote_type
       ORDER BY c.comment_id DESC
   """, nativeQuery = true)
    List<CommentProjection> findNewestRootComments(
            @Param("postId") Long postId,
            @Param("currentUserId") Long currentUserId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query(value = """
        SELECT
           c.comment_id AS commentId,
           c.comment_content AS commentContent,
           c.comment_path AS commentPath,
           c.parent_id as parentId,
           c.is_deleted AS isDeleted,
           c.created_at AS createdAt,
           c.updated_at AS updatedAt,
           c.post_id AS postId,
           u.user_id AS userId,
           u.user_name AS username,
           u.email as email,
           u.avatar_url AS avatarUrl,
           c.upvotes AS upvotes,
           c.downvotes AS downvotes,
           c.score AS score,
           cv.vote_type AS userVote,
           COUNT(DISTINCT r.comment_id) AS replyCount
       FROM comments c
       JOIN users u ON c.user_id = u.user_id
       LEFT JOIN comments r 
              ON r.post_id = c.post_id
             AND r.comment_path LIKE CONCAT(c.comment_path, c.comment_id, '/%')
             AND r.comment_id <> c.comment_id
       LEFT JOIN comment_votes cv 
              ON cv.comment_id = c.comment_id AND cv.user_id = :currentUserId
       WHERE c.post_id = :postId
         AND c.is_deleted = false
         AND (c.parent_id IS NULL OR c.comment_path = '/')
         -- Logic Composite Cursor: 
         -- Nhỏ hơn điểm cursorScore HOẶC (Bằng điểm cursorScore NHƯNG cũ hơn cursorId)
         AND (:cursorScore IS NULL OR c.score < :cursorScore OR (c.score = :cursorScore AND c.comment_id < :cursorId))
       GROUP BY c.comment_id, u.user_id, u.user_name, u.email, u.avatar_url, cv.vote_type
       ORDER BY c.score DESC, c.comment_id DESC
    """, nativeQuery = true)
    List<CommentProjection> findTopRootComments(
            @Param("postId") Long postId,
            @Param("currentUserId") Long currentUserId,
            @Param("cursorScore") Long cursorScore,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    Optional<CommentEntity> findByCommentIdAndIsDeletedFalse(Long commentId);

    @Query(value = """
        SELECT
            c.comment_id AS commentId,
            c.comment_content AS commentContent,
            c.comment_path AS commentPath,
            c.parent_id as parentId,
            c.is_deleted AS isDeleted,
            c.created_at AS createdAt,
            c.updated_at AS updatedAt,
            c.post_id AS postId,
            u.user_id AS userId,
            u.user_name AS username,
            u.email as email,
            u.avatar_url AS avatarUrl,
            c.upvotes AS upvotes,
            c.downvotes AS downvotes,
            c.score AS score,
            cv.vote_type AS userVote,
            COUNT(DISTINCT r.comment_id) AS replyCount
          FROM comments c
          JOIN users u ON c.user_id = u.user_id
          LEFT JOIN comments r
                 ON r.post_id = c.post_id
                AND r.comment_path LIKE CONCAT(c.comment_path, c.comment_id, '/%')
                AND r.comment_id <> c.comment_id
          LEFT JOIN comment_votes cv
              ON cv.comment_id = c.comment_id
              AND cv.user_id = :currentUserId
          WHERE c.post_id = :postId
            AND c.parent_id = :parentId
            AND c.is_deleted = false
          GROUP BY c.comment_id, u.user_id, u.user_name, u.email, u.avatar_url, cv.vote_type
          ORDER BY c.comment_id ASC;
    """, nativeQuery = true,
    countQuery = """
            SELECT count(*)
            FROM comments c
            WHERE c.post_id = :postId
              AND c.parent_id = :parentId
              AND c.is_deleted = false
            """)
    Page<CommentProjection> findChildCommentsWithReplyCountByPostId(
            @Param("postId") Long postId,
            @Param("parentId") Long parentId,
            @Param("currentUserId") Long currentUserId,
            Pageable pageable
    );

    Long countByPostEntity(PostEntity post);

    Long countByParentId(Long commentId);

    @Query(value = """
       SELECT
           c.comment_id AS commentId,
           c.comment_content AS commentContent,
           c.comment_path AS commentPath,
           c.parent_id as parentId,
           c.is_deleted AS isDeleted,
           c.created_at AS createdAt,
           c.updated_at AS updatedAt,
           c.post_id AS postId,
           u.user_id AS userId,
           u.user_name AS username,
           u.avatar_url AS avatarUrl,
           c.upvotes AS upvotes,
           c.downvotes AS downvotes,
           c.score AS score,
           cv.vote_type AS userVote,
           COUNT(DISTINCT r.comment_id) AS replyCount
       FROM comments c
       JOIN users u ON c.user_id = u.user_id
       LEFT JOIN comments r
              ON r.post_id = c.post_id
             AND r.comment_path LIKE CONCAT(c.comment_path, c.comment_id, '/%')
             AND r.comment_id <> c.comment_id
       LEFT JOIN comment_votes cv
             ON cv.comment_id = c.comment_id
             AND cv.user_id = :currentUserId
       WHERE c.comment_id IN :commentIds
         AND c.is_deleted = false
       GROUP BY c.comment_id, u.user_id, u.user_name, u.avatar_url, cv.vote_type
    """, nativeQuery = true)
    List<CommentProjection> findCommentContextByIds(
            @Param("commentIds") List<Long> commentIds,
            @Param("currentUserId") Long currentUserId
    );
}
