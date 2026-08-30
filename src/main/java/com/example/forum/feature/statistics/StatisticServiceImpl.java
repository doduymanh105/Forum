package com.example.forum.feature.statistics;

import com.example.forum.feature.comment.CommentRepository;
import com.example.forum.feature.post.PostRepository;
import com.example.forum.feature.post.dto.PostResponseDto;
import com.example.forum.feature.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticsService{

    private final UserRepository userRepo;
    private final PostRepository postRepo;
    private final CommentRepository commentRepo;

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
