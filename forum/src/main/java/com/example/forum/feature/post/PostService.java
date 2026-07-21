package com.example.forum.feature.post;

import com.example.forum.common.dto.CursorResponse;
import com.example.forum.feature.post.dto.CreatePostRequest;
import com.example.forum.feature.post.dto.UpdatePostRequest;
import com.example.forum.common.dto.PagedResponse;
import com.example.forum.feature.post.dto.PostResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostService {
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    PostResponseDto createPost(CreatePostRequest request);

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    PostResponseDto getPost(Long postId);

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    PagedResponse<PostResponseDto> getPostByUser (
            Long userId,
            String keyword,
            Pageable pageable
    );


    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    PagedResponse<PostResponseDto> getPosts (
            int page,
            int size,
            String sortBy,
            String sortDirect,
            String keyword
    );

    CursorResponse<PostResponseDto> getNewsfeed(String cursor, int size);

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    PostResponseDto updatePost(Long postId,UpdatePostRequest request);

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    void softDeletePost(Long id);

    @PreAuthorize("hasAnyRole('ADMIN')")
    void hardDeletePost(Long id);

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    void removeMediaFromPost(Long postId, Long mediaId);

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    PostResponseDto addMediaToPost(Long postId, List<MultipartFile> files);
}
