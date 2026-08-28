package com.example.forum.feature.follow;

import com.example.forum.core.exception.AppException;
import com.example.forum.core.exception.ErrorCode;
import com.example.forum.feature.user.UserSummaryProjection;
import com.example.forum.common.dto.PagedResponse;
import com.example.forum.feature.user.dto.UserSummaryDto;
import com.example.forum.domain.Enum.EventType;
import com.example.forum.domain.Follow;
import com.example.forum.domain.FollowId;
import com.example.forum.domain.NotificationEvent;
import com.example.forum.domain.UserEntity;
import com.example.forum.feature.user.UserRepository;
import com.example.forum.common.utils.SecurityUtils;
import com.example.forum.feature.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final SecurityUtils securityService;
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void followUser(Long followingId) {

        UserEntity following = userRepository.findById(followingId)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));

        UserEntity follower = securityService.getCurrentUser(); // user
        Long currentFollowerId= follower.getUserId();

        if(currentFollowerId==followingId){
            throw new AppException(ErrorCode.CANT_FOLLOW_YOURSELF);
        }

        FollowId followId = new FollowId(currentFollowerId, followingId);
        if(!followRepository.existsById(followId)){
            Follow newFollow = new Follow(followId,follower,following, LocalDateTime.now());
            followRepository.save(newFollow);
        } else {
            throw new AppException(ErrorCode.ALREADY_FOLLOWED);
        }

        NotificationEvent newNotificationEvent = notificationService.createEvent(
                EventType.NEW_FOLLOWER,
                follower,
                null,
                following.getUserId(),
                "USER");

        notificationService.notifyFollowers(newNotificationEvent);

    }

    @Override
    public PagedResponse<UserSummaryDto> getFollowers(int page, int size,String keyword) {
        UserEntity follower = securityService.getCurrentUser(); // user
        Long currentFollowerId= follower.getUserId();

        if (keyword == null) {
            keyword = "";
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<UserSummaryProjection> listOfFollowerPage = followRepository.findFollowerByFollowingIdAndUsername(currentFollowerId,keyword,pageable);
        List<UserSummaryDto> listOfFollowers= listOfFollowerPage.getContent().stream()
                .map(f -> new UserSummaryDto(
                        f.getUserId(),
                        f.getUserName(),
                        f.getEmail(),
                        f.getAvatarUrl()
                ))
                .collect(Collectors.toList());

        return new PagedResponse<>(
                listOfFollowers,
                listOfFollowerPage.getNumber(),
                listOfFollowerPage.getSize(),
                listOfFollowerPage.getTotalElements(),
                listOfFollowerPage.getTotalPages(),
                listOfFollowerPage.isLast()
        );
    }

    @Override
    public PagedResponse<UserSummaryDto> getFollowings(int page, int size, String keyword) {
        UserEntity following = securityService.getCurrentUser(); //this user
        Long currentFollowingId= following.getUserId();

        if (keyword == null) {
            keyword = "";
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<UserSummaryProjection> listOfFollowingWithPage = followRepository.findFollowingUserIdByFollowerUserId(currentFollowingId,keyword,pageable);
        List<UserSummaryDto> listOfFollowings= listOfFollowingWithPage.getContent().stream()
                .map(f -> new UserSummaryDto(
                        f.getUserId(),
                        f.getUserName(),
                        f.getEmail(),
                        f.getAvatarUrl()
                ))
                .collect(Collectors.toList());

        return new PagedResponse<>(
                listOfFollowings,
                listOfFollowingWithPage.getNumber(),
                listOfFollowingWithPage.getSize(),
                listOfFollowingWithPage.getTotalElements(),
                listOfFollowingWithPage.getTotalPages(),
                listOfFollowingWithPage.isLast()
        );
    }

    @Override
    public int getNumberOfFollower(Long followingId) {
        getUserOrThrow(followingId);
        return followRepository.countFollowers(followingId);
    }

    @Override
    public int getNumberOfFollowing(Long followerId) {
        getUserOrThrow(followerId);
        return followRepository.countFollowings(followerId);
    }

    @Override
    @Transactional
    public void unfollow(Long id) {

        UserEntity follower = securityService.getCurrentUser(); // user
        Long currentFollowerId= follower.getUserId();

        FollowId followId = new FollowId(currentFollowerId, id);

        Optional<Follow> existingFollowId = followRepository.findById(followId);

        if(existingFollowId.isPresent()){
            Follow existFollow= existingFollowId.get();
            followRepository.delete(existFollow);
        } else  {
            throw new AppException(ErrorCode.HAVE_NOT_FOLLOW);
        }
    }

    @Override
    @Transactional
    public void removeFollower(Long id) {

        getUserOrThrow(id);

        UserEntity currentUser = securityService.getCurrentUser();
        Long currentUserId = currentUser.getUserId();

        FollowId followId = new FollowId(id, currentUserId);

        Optional<Follow> existingFollow = followRepository.findById(followId);
        if (existingFollow.isPresent()) {
            followRepository.delete(existingFollow.get());
        } else {
            throw new AppException(ErrorCode.USER_HAVE_NOT_FOLLOW);
        }
    }

    @Override
    public PagedResponse<UserSummaryDto> findMutualFriends(int page, int size, String keyword) {
        Long currentUserId = securityService.getCurrentUser().getUserId();
        String searchKeyword = (keyword == null) ? "" : keyword;

        Pageable pageable = PageRequest.of(page, size);
        Page<UserSummaryProjection> friendPage = followRepository.findMutualFriends(currentUserId, searchKeyword, pageable);

        List<UserSummaryDto> friends = friendPage.getContent().stream()
                .map(f -> new UserSummaryDto(f.getUserId(), f.getUserName(), f.getEmail(), f.getAvatarUrl()))
                .toList();

        return new PagedResponse<>(
                friends, friendPage.getNumber(), friendPage.getSize(),
                friendPage.getTotalElements(), friendPage.getTotalPages(), friendPage.isLast()
        );
    }

    private UserEntity getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
