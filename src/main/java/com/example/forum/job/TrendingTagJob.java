package com.example.forum.job;

import com.example.forum.common.constant.AppConstants;
import com.example.forum.feature.tag.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrendingTagJob {

    private final TagService tagService;

    @Scheduled(fixedRate = AppConstants.TAG_JOB_SCHEDULE)
    public void syncTopTagsToRedis() {
        log.info("[CRON-JOB] Start refresh trending tag cache");

        tagService.refreshTrendingTag();

        log.info("[CRON-JOB] Synchronize Top Tags Success");
    }

}
