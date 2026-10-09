package com.example.forum.feature.post.dto;

import com.example.forum.domain.Enum.PostStatus;
import com.example.forum.domain.MediaEntity;
import com.example.forum.feature.tag.dto.TagDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDto {
    private Long postId;
    private String postTitle;
    private String postContent;
    private String thumbnailUrl;
    private List<MediaEntity> mediaEntityList;
    private Long upvotes;
    private Long downvotes;
    private Long countedViews;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private PostStatus postStatus;

//    private UserSummaryDto creator;
    private Long creatorId;
    private String creatorName;
    private String creatorAvatarUrl;

    private Set<TagDto> tags;

    private Long commentCount;
    private Integer timeRead;
    private String isVoted;
    private Boolean isSaved;
}
