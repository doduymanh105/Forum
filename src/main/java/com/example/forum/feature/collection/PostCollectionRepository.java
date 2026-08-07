package com.example.forum.feature.collection;

import com.example.forum.domain.PostCollection;
import com.example.forum.domain.PostCollectionId;
import com.example.forum.domain.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostCollectionRepository extends JpaRepository<PostCollection, PostCollectionId> {

    @Query("""
            SELECT p
            FROM PostCollection pc
            JOIN pc.postEntity p
            WHERE pc.saveCollection.id = :collectionId
            AND LOWER(p.postTitle) LIKE LOWER(CONCAT('%', :title, '%'))
            """)
    List<PostEntity> getPostsInCollection(
            @Param("collectionId")Long collectionId,
            @Param("title") String title
    );

    @Query("""
    SELECT p
    FROM PostCollection pc
    JOIN pc.postEntity p
    WHERE pc.saveCollection.userEntity.userId = :userId
    AND LOWER(p.postTitle) LIKE LOWER(CONCAT('%', :title, '%'))
    """)
    List<PostEntity> findSavedPostsByUserAndTitle(
            @Param("title") String title,
            @Param("userId") Long userId
    );

}
