package com.example.forum.feature.follow;

import com.example.forum.common.service.cache.CacheService;
import com.example.forum.core.config.RabbitMqConfig;
import com.example.forum.domain.Enum.EventType;
import com.example.forum.domain.Follow;
import com.example.forum.domain.FollowId;
import com.example.forum.domain.NotificationEvent;
import com.example.forum.domain.UserEntity;
import com.example.forum.feature.follow.dto.FollowMessage;
import com.example.forum.feature.follow.dto.PendingNotification;
import com.example.forum.feature.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class FollowWorker {

    private final FollowRepository followRepository;
    private final NotificationService notificationService;
    private final CacheService cacheService;

    @Transactional
    @RabbitListener(queues = RabbitMqConfig.FOLLOW_QUEUE, containerFactory = "batchContainerFactory")
    public void handleFollowBatch(List<FollowMessage> messages) {
        log.info("[WORKER] Start processing follow batch with {}", messages.size());

        if(messages.isEmpty()) return;

        Map<String, FollowMessage> distinctFollows =  new HashMap<>();
        for(FollowMessage message :messages ){
            String key = message.followerId() + "-" + message.followingId();
            distinctFollows.put(key, message);
        }

        List<Long> followerIds = distinctFollows.values().stream()
                .map(followMessage -> followMessage.followerId())
                .toList();

        List<Long> followingIds = distinctFollows.values().stream()
                .map(followMessage -> followMessage.followingId())
                .toList();

        List<Follow> existingFollows = followRepository.findByFollowerUserIdInAndFollowingUserIdIn(followerIds, followingIds);

        Map<String, Follow> existingFollowMap = existingFollows
                .stream().collect(Collectors.toMap(
                        f -> f.getFollower().getUserId() + "-" + f.getFollowing().getUserId(),
                        f -> f
                ));


        List<Follow> followToSave = new ArrayList<>();
        List<Follow> followToDelete = new ArrayList<>();
        Map<Long, Long> followerDelta = new HashMap<>();
        Map<Long, Long> followingDelta = new HashMap<>();

        List<PendingNotification> pendingNotifications = new ArrayList<>();

        for(FollowMessage msg : distinctFollows.values()){
            Follow existingFollow = existingFollowMap.get(
                    msg.followerId() + "-" + msg.followingId()
            );
            if(existingFollow == null && msg.action().equalsIgnoreCase("FOLLOW")){

                FollowId followId = new FollowId(msg.followerId(), msg.followingId());
                Follow newFollow = Follow.builder()
                        .followId(followId)
                        .follower(UserEntity.builder().userId(msg.followerId()).build())
                        .following(UserEntity.builder().userId(msg.followingId()).build())
                        .build();

                followToSave.add(newFollow);
                NotificationEvent newNotificationEvent = notificationService.createEvent(
                        EventType.NEW_FOLLOWER,
                        UserEntity.builder().userId(msg.followerId()).userName(msg.creatorName()).build(),
                        msg.creatorName() + " followed you",
                        msg.followingId(),
                        "USER");
                pendingNotifications.add(new PendingNotification(msg.followingId(), newNotificationEvent));
                followerDelta.merge(msg.followingId(), 1L, Long::sum);
                followingDelta.merge(msg.followerId(), 1L, Long::sum);
            } else if (msg.action().equalsIgnoreCase("UNFOLLOW") && existingFollow != null) {
                followToDelete.add(existingFollow);
                followerDelta.merge(msg.followingId(), -1L, Long::sum);
                followingDelta.merge(msg.followerId(), -1L, Long::sum);

            }
        }

        if (!followToSave.isEmpty()) followRepository.saveAll(followToSave);
        if (!followToDelete.isEmpty()) followRepository.deleteAll(followToDelete);

        for (Map.Entry<Long, Long> entry : followerDelta.entrySet()) {
            String redisKey = "user:" + entry.getKey() + ":followers";
            if (cacheService.get(redisKey) != null) {
                cacheService.increment(redisKey, entry.getValue());
            }
        }
        for (Map.Entry<Long, Long> entry : followingDelta.entrySet()) {
            String redisKey = "user:" + entry.getKey() + ":following";
            if (cacheService.get(redisKey) != null) {
                cacheService.increment(redisKey, entry.getValue());
            }
        }

        for(PendingNotification pNoti : pendingNotifications){
            notificationService.notifySpecificUser(UserEntity.builder().userId(pNoti.targetUserId()).build(), pNoti.notificationEvent());
        }

        log.info("[WORKER] Completed follow batch. Save: {}, Delete: {}, Notifications: {}",
                followToSave.size(), followToDelete.size(), pendingNotifications.size());
    }

}
