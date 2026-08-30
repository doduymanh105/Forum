package com.example.forum.feature.statistics;

import com.example.forum.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/forum/statistics")
@RequiredArgsConstructor
public class StatisticController {

    private final StatisticsService statisticsService;

    @GetMapping("/")
    public ResponseEntity<ApiResponse<OverviewStatsResponse>> getOverviews(){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Overviews"
                , statisticsService.getOverviewStatistics())
        );
    }
}
