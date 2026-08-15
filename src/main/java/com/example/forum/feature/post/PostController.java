package com.example.forum.feature.post;

import com.example.forum.common.dto.CursorResponse;
import com.example.forum.common.utils.SecurityUtils;
import com.example.forum.core.annotation.RateLimit;
import com.example.forum.feature.post.dto.*;
import com.example.forum.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
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
    private final SecurityUtils securityUtils;

    @RateLimit(capacity = 5, time = 1)
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

    @RateLimit(capacity = 100, time = 1)
    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostById(@PathVariable Long postId) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "get post by id successfully",
                        postService.getPost(postId)
                )
        );
    }

    @RateLimit(capacity = 100, time = 1)
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

    @RateLimit(capacity = 100, time = 1)
    @GetMapping("/all")
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

    @RateLimit(capacity = 100, time = 1)
    @GetMapping("/newsfeed")
    public ResponseEntity<?> getNewFeed(
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String cursor
    ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "get newsfeed",
                        postService.getNewsfeed(cursor,securityUtils.getCurrentUser(), size)
                )
        );
    }

    @RateLimit(capacity = 100, time = 1)
    @GetMapping("/search")
    public ResponseEntity<?> searchPosts(
            @ModelAttribute PostFilterRequest request,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "0") int page
            ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Search post success"
                , postService.searchPost(request, page, size))
        );
    }

    @RateLimit(capacity = 20, time = 1)
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

    @RateLimit(capacity = 20, time = 1)
    @PatchMapping("/{id}/soft-delete")
    public ResponseEntity<?> softDeletePost(@PathVariable Long id){
        postService.softDeletePost(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Post is temporaty deleted!",
               null
        ));
    }

    @RateLimit(capacity = 20, time = 1)
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

    @RateLimit(capacity = 20, time = 1)
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

    @RateLimit(capacity = 20, time = 1)
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

    @RateLimit(capacity = 5, time = 5)
    @PostMapping("/{postId}/summary")
    @Operation(
            summary = "AI Summary",
            description = "Automatically summary by OpenAI. Used DB summary if summary already made"
    )
    public ResponseEntity<ApiResponse<String>> getPostSummary(
            @PathVariable Long postId
    ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Summary get"
                , postService.getSummaryForPost(postId))
        );
    }

    @RateLimit(capacity = 5, time = 1)
    @PostMapping("/recommend-tags")
    @Operation(
            summary = "AI Recommend Tags",
            description = "Analyzes the provided post content and recommends up to 3 suitable tags from the system's predefined list."
    )
    public ResponseEntity<ApiResponse<List<String>>> recommendTagsForPost(
            @Valid @RequestBody PostRecommendTagRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Recommended tags"
                , postService.recommendTagsForContent(request))
        );
    }
}
