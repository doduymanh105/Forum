package com.example.forum.feature.statistics;

import com.example.forum.common.dto.ApiResponse;
import com.example.forum.feature.statistics.dto.ChartDataPoint;
import com.example.forum.feature.statistics.dto.LeaderBoardResponse;
import com.example.forum.feature.statistics.dto.OverviewStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

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
    @GetMapping("/userGrow")
    public ResponseEntity<ApiResponse<List<ChartDataPoint>>> getUserGrow(
            @RequestParam LocalDate startTime,
            @RequestParam(required = false) LocalDate endTime
            ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "User grow get"
                        , statisticsService.getUserGrowthChart(startTime, endTime)
                )
        );
    }

    @GetMapping("/postGrow")
    public ResponseEntity<ApiResponse<List<ChartDataPoint>>> getPostGrow(
            @RequestParam LocalDate startTime,
            @RequestParam(required = false) LocalDate endTime
    ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Post grow get"
                        , statisticsService.getPostGrowthChart(startTime, endTime)
                )
        );
    }

    @GetMapping("/leader-board")
    public ResponseEntity<ApiResponse<LeaderBoardResponse>> getLeaderBoard(
            @RequestParam LocalDate startTime,
            @RequestParam(required = false) LocalDate endTime
    ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "leader board get"
                        , statisticsService.getLeaderBoard(startTime, endTime)
                )
        );
    }
}
