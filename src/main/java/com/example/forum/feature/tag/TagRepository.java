package com.example.forum.feature.tag;

import com.example.forum.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag,Long> {

    @Query(value = """
            SELECT t.tag_id as tagId,
            t.tag_name as tagName,
            SUM((COALESCE(p.upvotes, 0) - COALESCE(p.downvotes,0))* 5 + COALESCE(p.comment_count,0)*10) as totalScore,
            COUNT(p) as totalPost
            FROM tags t
            JOIN post_tags pt ON t.tag_id = pt.tag_id
            JOIN post_entity p ON p.post_id = pt.post_id
            WHERE p.created_at >= :sinceDate
            AND p.created_at <= :endDate
            GROUP BY t.tag_id, t.tag_name
            ORDER BY totalScore DESC
            LIMIT :limit
            """,  nativeQuery = true)
    List<TrendingTagProjection> getTrendingTags(
            @Param("sinceDate") LocalDateTime sinceDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("limit") int limit
    );
}
