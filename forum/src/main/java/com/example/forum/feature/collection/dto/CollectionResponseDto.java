package com.example.forum.feature.collection.dto;

import com.example.forum.domain.UserEntity;
import lombok.Builder;

import java.time.LocalDateTime;
@Builder
public class CollectionResponseDto {
    private Long id;
    private String name;
    private UserEntity userEntity;
    private Long totalElements;
    private String thumbnailUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
