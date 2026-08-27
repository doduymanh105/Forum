package com.example.forum.feature.tag;

import com.example.forum.common.constant.AppConstants;
import com.example.forum.domain.Tag;
import com.example.forum.feature.tag.dto.TrendingTagDto;
import com.example.forum.feature.tag.dto.TrendingTagRequest;
import com.example.forum.feature.tag.dto.TrendingTagResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagServiceImpl implements TagService {
    private final TagRepository tagRepository;

    @Override
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    @Override
    @Cacheable(value = "topTags", key = "'trending'")
    public TrendingTagResponse getTrendingTag() {
        LocalDateTime sinceTime = LocalDateTime.now().minusDays(AppConstants.DEFAULT_DAYS);
        var projections = tagRepository.getTrendingTags(sinceTime, AppConstants.DEFAULT_LIMIT);
        List<TrendingTagDto> listDto= projections.stream()
                .map(p -> new TrendingTagDto(
                        p.getTagId(),
                        p.getTagName(),
                        p.getTotalScore())
                ).toList();
        return new TrendingTagResponse(listDto);
    }

    @Override
    @CachePut(value = "topTags", key = "'trending'")
    public TrendingTagResponse refreshTrendingTag() {
        LocalDateTime sinceTime = LocalDateTime.now().minusDays(AppConstants.DEFAULT_DAYS);
        var projections = tagRepository.getTrendingTags(sinceTime, AppConstants.DEFAULT_LIMIT);

        List<TrendingTagDto> listDto= projections.stream()
                .map(p -> new TrendingTagDto(
                        p.getTagId(),
                        p.getTagName(),
                        p.getTotalScore())
                ).toList();
        return new TrendingTagResponse(listDto);
    }
}
