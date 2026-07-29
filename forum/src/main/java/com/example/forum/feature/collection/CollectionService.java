package com.example.forum.feature.collection;

import com.example.forum.feature.collection.dto.*;
import com.example.forum.feature.post.dto.PostResponseDto;

import java.util.List;

public interface CollectionService {

    CollectionResponseDto createCollection(CreateCollectionRequest request);

    CollectionResponseDto updateCollection(UpdateCollectionRequest request);

    List<CollectionResponseDto> getAllCollections();

    CollectionContentResponse getCollectionById(Long collectionId, String keyword);

    void deleteCollection(Long collectionId);

    CollectionResponseDto addPostToCollection(Long collectionId, Long postId);

    CollectionResponseDto removePostFromCollection(Long collectionId, Long postId);

    List<PostPreviewDto> searchSaved(String title);
}
