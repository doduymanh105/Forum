package com.example.forum.feature.tag;

import com.example.forum.common.constant.AppConstants;
import com.example.forum.core.exception.AppException;
import com.example.forum.core.exception.ErrorCode;
import com.example.forum.domain.Tag;
import com.example.forum.feature.tag.dto.TrendingTagDto;
import com.example.forum.feature.tag.dto.TrendingTagRequest;
import com.example.forum.feature.tag.dto.TrendingTagResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagServiceImpl implements TagService {
    private final TagRepository tagRepository;


//    TODO: tách hàm lấy top trending tag admin và user

    @Override
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    @Override
    @Cacheable(value = "topTags", key = "'trending'")
    public TrendingTagResponse getTrendingTag() {
        LocalDateTime sinceTime = LocalDateTime.now().minusDays(AppConstants.DEFAULT_DAYS);
        LocalDateTime endTime = LocalDateTime.now();
        var projections = tagRepository.getTrendingTags(sinceTime, endTime,AppConstants.DEFAULT_LIMIT);
        List<TrendingTagDto> listDto= projections.stream()
                .map(p -> new TrendingTagDto(
                        p.getTagId(),
                        p.getTagName(),
                        p.getTotalScore(),
                        p.getTotalPost()
                        )
                ).toList();
        return new TrendingTagResponse(listDto);
    }

    @Override
    public TrendingTagResponse getTopTagsInRange(LocalDate startDate, LocalDate endDate) {

        if (endDate == null) endDate = LocalDate.now();
        if (startDate == null) startDate = endDate.minusDays(AppConstants.DEFAULT_DAYS);

        if (startDate.isAfter(endDate)) {
            throw new AppException(ErrorCode.TIME_CONFLICT);
        }
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        var projections = tagRepository.getTrendingTags(startDateTime, endDateTime,AppConstants.DEFAULT_LIMIT);
        List<TrendingTagDto> listDto= projections.stream()
                .map(p -> new TrendingTagDto(
                                p.getTagId(),
                                p.getTagName(),
                                p.getTotalScore(),
                                p.getTotalPost()
                        )
                ).toList();
        return new TrendingTagResponse(listDto);
    }

    @Override
    @CachePut(value = "topTags", key = "'trending'")
    public TrendingTagResponse refreshTrendingTag() {
        LocalDateTime sinceTime = LocalDateTime.now().minusDays(AppConstants.DEFAULT_DAYS);
        LocalDateTime endTime = LocalDateTime.now();
        var projections = tagRepository.getTrendingTags(sinceTime, endTime,AppConstants.DEFAULT_LIMIT);

        List<TrendingTagDto> listDto= projections.stream()
                .map(p -> new TrendingTagDto(
                        p.getTagId(),
                        p.getTagName(),
                        p.getTotalScore(),
                        p.getTotalPost())
                ).toList();
        return new TrendingTagResponse(listDto);
    }
}
