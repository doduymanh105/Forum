package com.example.forum.feature.collection;

import com.example.forum.domain.SaveCollection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CollectionRepository extends JpaRepository<SaveCollection, Long> {
    List<SaveCollection> findAllByUserEntityUserId(Long userId);

    int countByUserEntityUserId(Long userId);

    boolean existsByIdAndUserEntityUserId(Long collectionId, Long userId);

    Optional<SaveCollection> findByIdAndIsDeletedFalse(Long collectionId);
}
