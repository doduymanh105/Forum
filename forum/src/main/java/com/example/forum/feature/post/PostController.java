package com.example.forum.feature.post;

import com.example.forum.common.dto.CursorResponse;
import com.example.forum.feature.post.dto.CreatePostRequest;
import com.example.forum.feature.post.dto.UpdatePostRequest;
import com.example.forum.common.dto.ApiResponse;
import com.example.forum.feature.post.dto.PostResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/forum/posts")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping(value ="/create")
    public ResponseEntity<?> createPost (
            @Valid @RequestBody CreatePostRequest request
            ) {
        PostResponseDto postResponse = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.created(
                        "Create post successfully",
                        postResponse
                )
        );

    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostById(@PathVariable Long postId) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "get post by id successfully",
                        postService.getPost(postId)
                )
        );
    }

    @GetMapping()
    public ResponseEntity<?> getPostByOwner(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "") String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
            ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "get post by user successfully",
                        postService.getPostByUser(userId, keyword, pageable)
                )
        );
    }

    @GetMapping("/search")
    public ResponseEntity<?> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirect,
            @RequestParam(defaultValue = "") String keyword
    ) {
        System.out.println("GET-getPosts");
        return ResponseEntity.ok(
                ApiResponse.success(
                        "get post by filter",
                        postService.getPosts(page, size, sortBy, sortDirect, keyword)
                )
        );
    }

    @GetMapping("/newsfeed")
    public ResponseEntity<?> getNewFeed(
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String cursor
    ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "get newsfeed",
                        postService.getNewsfeed(cursor, size)
                )
        );
    }

    @PatchMapping("/{id}/update")
    public ResponseEntity<?> updatePost(
            @PathVariable Long id,
            @RequestBody UpdatePostRequest request
            ){
        return ResponseEntity.ok(ApiResponse.success(
                "Updated",
                postService.updatePost(id, request)
        ));
    }

    @PatchMapping("/{id}/soft-delete")
    public ResponseEntity<?> softDeletePost(@PathVariable Long id){
        postService.softDeletePost(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Post is temporaty deleted!",
               null
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> hardDeletePost(@PathVariable Long id){
        postService.hardDeletePost(id);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Post is permanently deleted",
                        null
                )
        );
    }

    @PostMapping(value = "/{postId}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addMediaToPost(
            @PathVariable Long postId,
            @RequestPart("files") List<MultipartFile> files
    ) {
        PostResponseDto updatedPost = postService.addMediaToPost(postId, files);

        return ResponseEntity.ok(
                ApiResponse.success( "Added media successfully", updatedPost)
        );
    }

    @DeleteMapping("/{postId}/media/{mediaId}")
    public ResponseEntity<?> removeMediaFromPost(
            @PathVariable Long postId,
            @PathVariable Long mediaId
    ) {
        postService.removeMediaFromPost(postId, mediaId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Remove media successfully",
                        null
                )
        );
    }
}
