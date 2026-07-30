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
    @Query(value = "SELECT * FROM comments where post_id=:postId AND comment_path LIKE :path order by created_at",
    nativeQuery = true)
    List<CommentEntity> findByPostIdAndPathLike(
            @Param("postId") Long postId,
            @Param("path") String path
    );

    @Query(value = """
    SELECT\s
       c.comment_id AS commentId,
       c.comment_content AS commentContent,
       c.comment_path AS commentPath,
       c.is_deleted AS isDeleted,
       c.created_at AS createdAt,
       c.updated_at AS updatedAt,
       c.post_id AS postId,
       u.user_id AS userId,
       u.user_name AS username,
       u.avatar_url AS avatarUrl,
       COUNT(r.comment_id) AS replyCount
   FROM comments c
   JOIN users u ON c.user_id = u.user_id
   LEFT JOIN comments r\s
          ON r.post_id = c.post_id
         AND r.comment_path LIKE CONCAT(c.comment_path, c.comment_id, '/%')
         AND r.comment_id <> c.comment_id
   WHERE c.post_id = :postId
     AND c.is_deleted = false
     AND (c.parent_id IS NULL OR c.comment_path = '/')
     AND (:cursor IS NULL OR c.comment_id < :cursor)
   GROUP BY c.comment_id, u.user_id, u.user_name, u.avatar_url
   ORDER BY c.comment_id DESC
""", nativeQuery = true)
    List<CommentProjection> findRootCommentsWithReplyCountByPostId(
            @Param("postId") Long postId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    Optional<CommentEntity> findByCommentIdAndIsDeletedFalse(Long commentId);

    @Query(value = """
    SELECT
          c.comment_id AS commentId,
          c.comment_content AS commentContent,
          c.comment_path AS commentPath,
          c.is_deleted AS isDeleted,
          c.created_at AS createdAt,
          c.updated_at AS updatedAt,
          c.post_id AS postId,
          u.user_id AS userId,
          u.user_name AS username,
          u.avatar_url AS avatarUrl,
          COUNT(r.comment_id) AS replyCount
      FROM comments c
      JOIN users u ON c.user_id = u.user_id
      LEFT JOIN comments r
             ON r.post_id = c.post_id
            AND r.comment_path LIKE CONCAT(c.comment_path, c.comment_id, '/%')
            AND r.comment_id <> c.comment_id
      WHERE c.post_id = :postId
        AND c.parent_id = :parentId
        AND c.is_deleted = false
      GROUP BY c.comment_id, u.user_id, u.user_name, u.avatar_url
      ORDER BY c.comment_id ASC;
""", nativeQuery = true,
    countQuery = """
            SELECT count(*)
            FROM comments c
            WHERE c.post_id = :postId
              AND c.parent_id = :parentId
              AND c.is_deleted = false
            """)
    Page<CommentProjection> findCommentsWithReplyCountByPostId(
            @Param("postId") Long postId,
            @Param("parentId") Long parentId,
            Pageable pageable
    );

    Long countByPostEntity(PostEntity post);

    Page<CommentEntity> findByPostEntity_PostIdAndParentIdIsNull(Long postId, Pageable pageable);

    Long countByParentId(Long commentId);

    List<CommentEntity> findByParentIdOrderByCreatedAtAsc(Long parentId);
}
