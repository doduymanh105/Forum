package com.example.forum.feature.follow;

import com.example.forum.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/forum/user")
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{followingId}/follow")
    ResponseEntity<?> follow(
           @PathVariable Long followingId
    ) {
        followService.followUser(followingId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Successfully, you have followed this user!"
                ));
    }

    @GetMapping("/me/follower")
    ResponseEntity<?> getFollowers(
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

    @GetMapping("/me/following")
    ResponseEntity<?> getFollowings(
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

    @GetMapping("/{userId}/follower/count")
    ResponseEntity<?> getNumberOfFollower(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Number of followers",
                followService.getNumberOfFollower(userId)
        ));
    }

    @GetMapping("/{userId}/following/count")
    ResponseEntity<?> getNumberOfFollowing(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                "Number of followings",
                        followService.getNumberOfFollowing(userId)
        ));
    }

    @DeleteMapping("/{followingId}/unfollow")
    ResponseEntity<?> unfollow(
            @PathVariable Long followingId
    ) {
        followService.unfollow(followingId);
        return ResponseEntity.ok(
                ApiResponse.success(
                "Successfully, you have unfollowed this user"
        ));
    }


    @DeleteMapping("/me/follower/{followerId}/remove")
    ResponseEntity<?> removeFollower(@PathVariable Long followerId) {
        followService.removeFollower(followerId);
        return ResponseEntity.ok(ApiResponse.success(
                "Successfully, the follower has been removed"
        ));
    }

}
