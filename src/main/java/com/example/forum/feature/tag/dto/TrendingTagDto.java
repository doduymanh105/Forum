package com.example.forum.feature.tag.dto;


import lombok.Builder;

@Builder
public record TrendingTagDto(
        Long tagId,
        String tagName,
        Long totalScore
) {}
