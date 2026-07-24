package com.example.forum.feature.post.dto;

import lombok.Data;

import java.util.List;

@Data
public class PostFilterRequest {
    private String keyword;
    private List<Long> tags;
}
