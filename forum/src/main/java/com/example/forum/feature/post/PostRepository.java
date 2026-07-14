package com.example.forum.feature.post;

import com.example.forum.domain.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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


}
