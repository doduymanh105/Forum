package com.example.forum.feature.post;

import com.example.forum.domain.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, Long> {

    Optional<PostEntity> findByPostId(Long id);

    Page<PostEntity> findByPostTitleContainingIgnoreCaseAndIsArchivedFalse(String keyword, Pageable pageable);

    Page<PostEntity> findByCreatorUserIdAndIsArchivedFalseAndPostTitleContainingIgnoreCase(
            Long userId,
            String keyword,
            Pageable pageable
    );

    @Query(
            """
                    select p from PostEntity p order by p.postId desc
                    """
    )
    List<PostEntity> getLatestPosts(
            Pageable pageable
    );

    @Query(
            """
                    select p from PostEntity p where p.postId < :cursor ORDER BY p.postId DESC
                    """
    )
    List<PostEntity> getPostsByCursor(
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Modifying
    @Query("UPDATE PostEntity p SET p.commentCount = p.commentCount + 1 WHERE p.postId = :postId")
    void incrementCommentCount(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE PostEntity p SET p.commentCount = p.commentCount - 1 WHERE p.postId = :postId AND p.commentCount > 0")
    void decrementCommentCount(@Param("postId") Long postId);



    @Query(
            value = """
                    WITH RankedPosts AS (
                        SELECT
                            p.*,
                                ( (COALESCE(p.upvotes,0) - (COALESCE(p.downvotes,0)) * 5 ))
                                + COALESCE(p.comment_count, 0) * 10
                                - (EXTRACT(EPOCH FROM current_timestamp - p.created_at) /3600.0 *2)
                                + (CASE WHEN f.following_id IS NOT NULL THEN 50 ELSE 0 END)
                            AS score
                        FROM post_entity p
                        LEFT JOIN follow f
                            ON f.following_id = p.creator_id
                            AND f.follower_id = :currentUserId
                        WHERE p.is_archived = false
                    )
                    
                    SELECT * FROM RankedPosts
                    WHERE (:cursorScore IS NULL)
                    OR( score < :cursorScore)
                    OR( score = :cursorScore AND post_id < :cursorId)
                    ORDER BY score DESC, post_id DESC
                    """,
            nativeQuery = true
    )
    List<PostEntity> getNewsfeedRanking(
            @Param("currentUserId") Long currentUserId,
            @Param(value = "cursorScore") Double cursorScore,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}
