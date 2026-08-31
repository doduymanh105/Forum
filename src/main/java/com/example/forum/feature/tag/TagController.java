package com.example.forum.feature.tag;

import com.example.forum.common.dto.ApiResponse;
import com.example.forum.core.annotation.RateLimit;
import com.example.forum.domain.Tag;
import com.example.forum.feature.tag.dto.TrendingTagDto;
import com.example.forum.feature.tag.dto.TrendingTagResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@io.swagger.v3.oas.annotations.tags.Tag(name = "Tag API")
@RestController
@RequestMapping("/forum/tags")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    @RateLimit(capacity = 100, time = 1)
    public ResponseEntity<List<Tag>> getAllTags() {
        List<Tag> tags = tagService.getAllTags();
        return ResponseEntity.ok(tags);
    }

    @RateLimit(capacity = 100, time = 1)
    @GetMapping("/trendingTags")
    public ResponseEntity<ApiResponse<TrendingTagResponse>> getTrendingTags(){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Trending tags get",
                tagService.getTrendingTag())
        );
    }
}