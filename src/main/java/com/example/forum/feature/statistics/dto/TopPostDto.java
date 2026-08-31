package com.example.forum.feature.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TopPostDto {
    private Long postId;
    private String title;
    private String contentPreview;
    private Long upvote;
    private Long downvote;
    private String creatorName;
    private String creatorAvatarUrl;
}
