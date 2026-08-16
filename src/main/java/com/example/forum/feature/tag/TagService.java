package com.example.forum.feature.tag;

import com.example.forum.domain.Tag;
import com.example.forum.feature.tag.dto.TrendingTagDto;
import com.example.forum.feature.tag.dto.TrendingTagRequest;

import java.util.List;

public interface TagService {
    List<Tag> getAllTags();
    List<TrendingTagDto> getTrendingTag();
    List<TrendingTagDto> refreshTrendingTag();
}
