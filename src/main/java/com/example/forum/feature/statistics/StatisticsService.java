package com.example.forum.feature.statistics;

import com.example.forum.feature.statistics.dto.ChartDataPoint;
import com.example.forum.feature.statistics.dto.LeaderBoardResponse;
import com.example.forum.feature.statistics.dto.OverviewStatsResponse;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDate;
import java.util.List;

public interface StatisticsService {

    @PreAuthorize("hasRole('ADMIN')")
    OverviewStatsResponse getOverviewStatistics();

    @PreAuthorize("hasRole('ADMIN')")
    List<ChartDataPoint> getUserGrowthChart(LocalDate startDate, LocalDate endDate);

    @PreAuthorize("hasRole('ADMIN')")
    List<ChartDataPoint> getPostGrowthChart(LocalDate startDate, LocalDate endDate);

    @PreAuthorize("hasRole('ADMIN')")
    LeaderBoardResponse getLeaderBoard(LocalDate startDate, LocalDate endDate);

}
