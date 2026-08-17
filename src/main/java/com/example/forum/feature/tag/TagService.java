package com.example.forum.feature.tag;

import com.example.forum.domain.Tag;
import com.example.forum.feature.tag.dto.TrendingTagDto;
import com.example.forum.feature.tag.dto.TrendingTagRequest;
import com.example.forum.feature.tag.dto.TrendingTagResponse;

import java.util.List;

public interface TagService {
    List<Tag> getAllTags();
    TrendingTagResponse getTrendingTag();
    TrendingTagResponse refreshTrendingTag();
}
