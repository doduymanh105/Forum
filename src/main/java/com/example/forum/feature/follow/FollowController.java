package com.example.forum.feature.follow;

import com.example.forum.common.dto.ApiResponse;
import com.example.forum.common.dto.PagedResponse;
import com.example.forum.core.annotation.RateLimit;
import com.example.forum.feature.user.dto.UserSummaryDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Follow API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/forum/user")
public class FollowController {

    private final FollowService followService;

    @RateLimit(capacity = 20, time = 1)
    @PostMapping("/{followingId}/follow")
    ResponseEntity<ApiResponse<?>> follow(
           @PathVariable Long followingId
    ) {
        followService.followUser(followingId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Successfully, you have followed this user!"
                ));
    }

    @RateLimit(capacity = 100, time = 1)
    @GetMapping("/me/follower")
    ResponseEntity<ApiResponse<PagedResponse<UserSummaryDto>>> getFollowers(
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size,
            @RequestParam( defaultValue = "", required = false) String keyword
    ) {
       return ResponseEntity.ok(
               ApiResponse.success(
                       "List of followers",
                       followService.getFollowers(page,size,keyword)
               )) ;
    }

    @RateLimit(capacity = 100, time = 1)
    @GetMapping("/me/following")
    ResponseEntity<ApiResponse<PagedResponse<UserSummaryDto>>> getFollowings(
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size,
            @RequestParam( defaultValue = "", required = false) String keyword
    ) {
       return ResponseEntity.ok(
               ApiResponse.success(
                       "List of followings",
                       followService.getFollowings(page,size,keyword)
               )) ;
    }

    @RateLimit(capacity = 100, time = 1)
    @GetMapping("/{userId}/follower/count")
    ResponseEntity<ApiResponse<Integer>> getNumberOfFollower(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Number of followers",
                followService.getNumberOfFollower(userId)
        ));
    }

    @RateLimit(capacity = 100, time = 1)
    @GetMapping("/{userId}/following/count")
    ResponseEntity<ApiResponse<Integer>> getNumberOfFollowing(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                "Number of followings",
                        followService.getNumberOfFollowing(userId)
        ));
    }

    @RateLimit(capacity = 100, time = 1)
    @DeleteMapping("/{followingId}/unfollow")
    ResponseEntity<ApiResponse<?>> unfollow(
            @PathVariable Long followingId
    ) {
        followService.unfollow(followingId);
        return ResponseEntity.ok(
                ApiResponse.success(
                "Successfully, you have unfollowed this user"
        ));
    }


    @RateLimit(capacity = 100, time = 1)
    @DeleteMapping("/me/follower/{followerId}/remove")
    ResponseEntity<ApiResponse<?>> removeFollower(@PathVariable Long followerId) {
        followService.removeFollower(followerId);
        return ResponseEntity.ok(ApiResponse.success(
                "Successfully, the follower has been removed"
        ));
    }

}
