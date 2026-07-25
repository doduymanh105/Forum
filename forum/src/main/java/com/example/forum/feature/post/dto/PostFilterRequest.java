package com.example.forum.feature.post.dto;

import com.example.forum.domain.Enum.SortType;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
public class PostFilterRequest {
    private String keyword;
    private List<Long> tags;
    private Long authorId;
    // for spring to parse data from URL request
    @DateTimeFormat(pattern = "yyyy-M-d")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-M-d")
    private LocalDate endDate;

    private Integer minUpvote;
    private SortType sortBy;
}
