package com.example.forum.feature.tag.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendingTagDto {
    private Long tagId;
    private String tagName;
    private Long totalScore;
}
