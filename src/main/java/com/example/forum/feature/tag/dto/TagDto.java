package com.example.forum.feature.tag.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TagDto {
    private Long tagId;
    private String tagName;
}
