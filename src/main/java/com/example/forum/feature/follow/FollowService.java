package com.example.forum.feature.follow;

import com.example.forum.common.dto.PagedResponse;
import com.example.forum.feature.user.dto.UserSummaryDto;
import org.springframework.security.access.prepost.PreAuthorize;

public interface FollowService {
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    void followUser(Long userId);

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    PagedResponse<UserSummaryDto> getFollowers(int page, int size, String keyword);

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    PagedResponse<UserSummaryDto> getFollowings(int page, int size, String keyword);

    int getNumberOfFollower(Long userId);
    int getNumberOfFollowing(Long userId);

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    void unfollow(Long followingId);
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    void removeFollower(Long followerId);

}
