package com.example.forum.feature.collection.dto;

import lombok.Data;

@Data
public class UpdateCollectionRequest {
    private Long collectionId;
    private String newName;
    private String newThumbnailUrl;
}
