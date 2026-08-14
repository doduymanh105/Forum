package com.example.forum.feature.collection;

import com.example.forum.common.dto.ApiResponse;
import com.example.forum.core.annotation.RateLimit;
import com.example.forum.feature.collection.dto.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Collection API")
@RestController
@RequestMapping("/forum/saved")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    @RateLimit(capacity = 10, time = 1)
    @PostMapping("/collections")
    public ResponseEntity<ApiResponse<CollectionResponseDto>> createCollection(
            @RequestBody CreateCollectionRequest request
    ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Collection created"
                , collectionService.createCollection(request))
        );
    }
    @RateLimit(capacity = 20, time = 1)
    @PatchMapping("/collections")
    public ResponseEntity<ApiResponse<CollectionResponseDto>> updateCollection(
            @RequestBody UpdateCollectionRequest request
    ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Collection Updated"
                        , collectionService.updateCollection(request))
        );
    }

    @RateLimit(capacity = 100, time = 1)
    @GetMapping("/collections")
    public ResponseEntity<ApiResponse<List<CollectionResponseDto>>> getCollections(){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "collections get"
                        , collectionService.getAllCollections())
        );
    }

    @RateLimit(capacity = 100, time = 1)
    @GetMapping("/collections/{id}")
    public ResponseEntity<ApiResponse<CollectionContentResponse>> getCollectionById(
           @PathVariable(name = "id") Long collectionId,
            @RequestParam(defaultValue = "") String keyword
    ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "collection gets"
                        , collectionService.getCollectionById(collectionId, keyword))
        );
    }

    @RateLimit(capacity = 20, time = 1)
    @DeleteMapping("/collections")
    public ResponseEntity<ApiResponse<?>> softDeleteCollection(
            @RequestParam Long id
    ){
        collectionService.deleteCollection(id);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "collections deleted"
                ));
    }

    @RateLimit(capacity = 20, time = 1)
    @PostMapping("/collections/saved")
    public ResponseEntity<ApiResponse<CollectionResponseDto>> addPostToCollection(
            @RequestParam Long collectionId,
            @RequestParam Long postId
    ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Post added",
                        collectionService.addPostToCollection(collectionId, postId)
                ));
    }

    @RateLimit(capacity = 20, time = 1)
    @DeleteMapping("/collections/saved")
    public ResponseEntity<ApiResponse<CollectionResponseDto>> removePostFromCollection(
            @RequestParam Long collectionId,
            @RequestParam Long postId
    ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Post removed",
                        collectionService.removePostFromCollection(collectionId, postId)
                ));
    }

    @RateLimit(capacity = 100, time = 1)
    @GetMapping("/collections/saved-search")
    public ResponseEntity<ApiResponse<List<PostPreviewDto>>> searchPost(
            @RequestParam(required = false) String title
            ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Saved search"
                , collectionService.searchSaved(title))
        );
    }
}


