package com.example.forum.feature.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class OverviewStatsResponse {
    Long totalUser;
    Long newUserToday;
    Long totalPosts;
    Long totalComments;
    Long bannedUsersCount;
    Long twoFactorEnabledCount;
    Double twoFactorAdoptionRate;
}
