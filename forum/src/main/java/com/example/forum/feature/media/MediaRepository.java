package com.example.forum.feature.media;

import com.example.forum.domain.MediaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaRepository extends JpaRepository<MediaEntity, Long> {
    List<MediaEntity> findByPostPostId(Long id);
}
