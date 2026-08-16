package com.example.forum.feature.tag.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendingTagRequest {
    LocalDateTime sinceTime;
    int limit;
}
