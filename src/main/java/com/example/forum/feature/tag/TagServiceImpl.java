package com.example.forum.feature.tag;

import com.example.forum.common.constant.AppConstants;
import com.example.forum.domain.Tag;
import com.example.forum.feature.tag.dto.TrendingTagDto;
import com.example.forum.feature.tag.dto.TrendingTagRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {
    private final TagRepository tagRepository;

    @Override
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    @Override
    @Cacheable(value = "topTags", key = "'trending'")
    public List<TrendingTagDto> getTrendingTag() {
        LocalDateTime sinceTime = LocalDateTime.now().minusDays(AppConstants.DEFAULT_DAYS);
        var projections = tagRepository.getTrendingTags(sinceTime, AppConstants.DEFAULT_LIMIT);
        return projections.stream()
                .map(p -> new TrendingTagDto(
                        p.getTagId(),
                        p.getTagName(),
                        p.getTotalScore())
                ).toList();
    }

    @Override
    @CachePut(value = "topTags", key = "'trending'")
    public List<TrendingTagDto> refreshTrendingTag() {
        LocalDateTime sinceTime = LocalDateTime.now().minusDays(AppConstants.DEFAULT_DAYS);
        var projections = tagRepository.getTrendingTags(sinceTime, AppConstants.DEFAULT_LIMIT);

        return projections.stream()
                .map(p -> new TrendingTagDto(
                        p.getTagId(),
                        p.getTagName(),
                        p.getTotalScore())
                ).toList();
    }
}
