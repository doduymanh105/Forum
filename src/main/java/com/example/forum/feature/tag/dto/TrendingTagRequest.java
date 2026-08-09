package com.example.forum.feature.tag.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TrendingTagRequest {
    LocalDateTime sinceTime;
    int limit;
}
