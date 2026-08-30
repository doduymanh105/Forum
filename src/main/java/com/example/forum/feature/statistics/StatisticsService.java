package com.example.forum.feature.statistics;

import org.springframework.security.access.prepost.PreAuthorize;

public interface StatisticsService {

    @PreAuthorize("hasRole('ADMIN')")
    OverviewStatsResponse getOverviewStatistics();
}
