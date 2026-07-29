package com.example.forum.feature.collection;

import com.example.forum.common.dto.ApiResponse;
import com.example.forum.feature.collection.dto.CreateCollectionRequest;
import com.example.forum.feature.collection.dto.UpdateCollectionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/forum/saved")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    @PostMapping("/collections")
    public ResponseEntity<?> createCollection(
            @RequestBody CreateCollectionRequest request
    ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Collection created"
                , collectionService.createCollection(request))
        );
    }

    @PatchMapping("/collections")
    public ResponseEntity<?> updateCollection(
            @RequestBody UpdateCollectionRequest request
    ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Collection Updated"
                        , collectionService.updateCollection(request))
        );
    }

    @GetMapping("/collections")
    public ResponseEntity<?> getCollections(){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "collections get"
                        , collectionService.getAllCollections())
        );
    }

    @GetMapping("/collections/{id}")
    public ResponseEntity<?> getCollectionById(
           @PathVariable(name = "id") Long collectionId,
            @RequestParam(defaultValue = "") String keyword
    ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "collection gets"
                        , collectionService.getCollectionById(collectionId, keyword))
        );
    }

    @DeleteMapping("/collections")
    public ResponseEntity<?> softDeleteCollection(
            @RequestParam Long id
    ){
        collectionService.deleteCollection(id);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "collections deleted"
                ));
    }

    @PostMapping("/collections/saved")
    public ResponseEntity<?> addPostToCollection(
            @RequestParam Long collectionId,
            @RequestParam Long postId
    ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Post added",
                        collectionService.addPostToCollection(collectionId, postId)
                ));
    }

    @DeleteMapping("/collections/saved")
    public ResponseEntity<?> removePostFromCollection(
            @RequestParam Long collectionId,
            @RequestParam Long postId
    ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Post removed",
                        collectionService.removePostFromCollection(collectionId, postId)
                ));
    }

    @GetMapping("/collections/saved-search")
    public ResponseEntity<?> searchPost(
            @RequestParam(required = false) String title
            ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Saved search"
                , collectionService.searchSaved(title))
        );
    }



}


