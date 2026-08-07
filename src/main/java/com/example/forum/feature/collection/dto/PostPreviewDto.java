package com.example.forum.feature.collection.dto;

import com.example.forum.domain.PostEntity;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostPreviewDto {
    private Long postId;
    private String title;
    private String previewContent;
    private String thumbnailUrl;

    public static PostPreviewDto mapToPostPreviewDto(PostEntity postEntity){

        String preview = postEntity.getPostContent();
        if(postEntity.getPostContent().length() >= 150){
            preview = postEntity.getPostContent().substring(0, 150);
        }

        return PostPreviewDto.builder()
                .postId(postEntity.getPostId())
                .title(postEntity.getPostTitle())
                .previewContent(preview)
                .thumbnailUrl(postEntity.getThumbnailUrl())
                .build();
    }
}
