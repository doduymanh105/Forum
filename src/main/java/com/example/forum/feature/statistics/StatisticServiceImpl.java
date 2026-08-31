package com.example.forum.feature.statistics;

import com.example.forum.common.constant.AppConstants;
import com.example.forum.core.exception.AppException;
import com.example.forum.core.exception.ErrorCode;
import com.example.forum.domain.PostEntity;
import com.example.forum.feature.collection.dto.PostPreviewDto;
import com.example.forum.feature.comment.CommentRepository;
import com.example.forum.feature.post.PostRepository;

import com.example.forum.feature.statistics.dto.*;
import com.example.forum.feature.tag.TagService;
import com.example.forum.feature.tag.dto.TrendingTagDto;
import com.example.forum.feature.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticsService{

    private final UserRepository userRepo;
    private final PostRepository postRepo;
    private final CommentRepository commentRepo;

    private final TagService tagService;

    @Override
    public List<ChartDataPoint> getUserGrowthChart(LocalDate startDate, LocalDate endDate) {

        if(endDate == null){
            endDate = LocalDate.now();
        }
        if(startDate.isAfter(endDate)){
            throw new AppException(ErrorCode.TIME_CONFLICT);
        }
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        LocalDateTime startDateTime = startDate.atStartOfDay();

        long days = ChronoUnit.DAYS.between(startDateTime, endDateTime) +1;

        List<Object[]> data = userRepo.countUsersGroupedByDate(startDateTime, endDateTime);
        Map<String, Long> dataMap = data.stream()
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> Long.valueOf(row[1].toString())
                ));


        List<ChartDataPoint> result = new ArrayList<>();
        for(int i =0; i < days; i++ ){
            LocalDate currentDate = startDate.plusDays(i);
            String dateStr = currentDate.toString();

            Long count = dataMap.getOrDefault(dateStr, 0L);

            result.add(new ChartDataPoint(
                    dateStr,
                    count
            ));
        }
        return result;
    }


    @Override
    public LeaderBoardResponse getLeaderBoard(LocalDate startDate, LocalDate endDate) {

        if (endDate == null) endDate = LocalDate.now();
        if (startDate == null) startDate = endDate.minusDays(7);

        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        LocalDateTime startDateTime = startDate.atStartOfDay();

        List<TrendingTagDto> trendingTags = tagService.getTopTagsInRange(startDate, endDate).getTags();

        List<TopUserProjection> topUsers = userRepo.getTopUsers(startDateTime,endDateTime);
        List<TopUserDto> topUserDto = topUsers.stream()
                .map(TopUserDto::mapToTopUserDto)
                .toList();

        List<PostEntity> topPostEntityList = postRepo.findTopPosts(startDateTime,endDateTime);
        List<PostPreviewDto> topPostDto = topPostEntityList.stream()
                .map(PostPreviewDto::mapToPostPreviewDto).toList();

        return LeaderBoardResponse.builder()
                .topTags(trendingTags)
                .topActiveUsers(topUserDto)
                .topPosts(topPostDto)
                .build();
    }

    @Override
    public List<ChartDataPoint> getPostGrowthChart(LocalDate startDate, LocalDate endDate) {

        if(endDate == null){
            endDate = LocalDate.now();
        }
        if(startDate.isAfter(endDate)){
            throw new AppException(ErrorCode.TIME_CONFLICT);
        }
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        LocalDateTime startDateTime = startDate.atStartOfDay();

        long days = ChronoUnit.DAYS.between(startDateTime, endDateTime) +1;

        List<Object[]> data = postRepo.countPostsGroupedByDate(startDateTime, endDateTime);
        Map<String, Long> dataMap = data.stream()
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> Long.valueOf(row[1].toString())
                ));


        List<ChartDataPoint> result = new ArrayList<>();
        for(int i =0; i < days; i++ ){
            LocalDate currentDate = startDate.plusDays(i);
            String dateStr = currentDate.toString();

            Long count = dataMap.getOrDefault(dateStr, 0L);

            result.add(new ChartDataPoint(
                    dateStr,
                    count
            ));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public OverviewStatsResponse getOverviewStatistics() {

        Long totalUsers = userRepo.countByIsDeletedFalse();

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

        Long newUserToday = userRepo.countByCreatedAtAfter(startOfToday);

        Long totalPosts = postRepo.countByIsArchivedFalse();

        Long totalComments = commentRepo.countByIsDeletedFalse();

        Long bannedUsersCount = 0L;

        Long twoFaEnabledCount = userRepo.countByIsDeletedFalseAndIsTwoFactorEnabledTrue();

        double twoFactorAdoptionRate = 0.0;
        if(totalUsers != 0){
            twoFactorAdoptionRate = ((double) twoFaEnabledCount /totalUsers)*100D;
        }

        return OverviewStatsResponse.builder()
                .totalUser(totalUsers)
                .newUserToday(newUserToday)
                .totalPosts(totalPosts)
                .totalComments(totalComments)
                .bannedUsersCount(bannedUsersCount)
                .twoFactorEnabledCount(twoFaEnabledCount)
                .twoFactorAdoptionRate(twoFactorAdoptionRate)
                .build();
    }
}
