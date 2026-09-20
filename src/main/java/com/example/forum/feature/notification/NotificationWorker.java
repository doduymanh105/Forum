package com.example.forum.feature.notification;

import com.example.forum.core.config.RabbitMqConfig;
import com.example.forum.core.exception.AppException;
import com.example.forum.core.exception.ErrorCode;
import com.example.forum.domain.Notification;
import com.example.forum.domain.NotificationEvent;
import com.example.forum.domain.UserEntity;
import com.example.forum.feature.follow.FollowRepository;
import com.example.forum.feature.notification.dto.FanoutNotificationMessage;
import com.example.forum.feature.notification.dto.NotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationWorker {

    private final FollowRepository followRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationEventRepository notificationEventRepository;
    private final WebsocketNotificationService websocketNotificationService;


    @Transactional
    @RabbitListener(queues = RabbitMqConfig.NOTIFICATION_QUEUE)
    public void notifyFollowers(FanoutNotificationMessage message) {
        log.info("[WORKER] fan-out notification STARTED");
        int page = 0;
        int batchSize = 2;

        Page<UserEntity> followersPage;

        NotificationEvent notificationEvent = notificationEventRepository.findById(message.eventId())
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        do {
            Pageable pageable = PageRequest.of(page, batchSize);
            followersPage = followRepository.findFollowersByAuthorId(message.authorId(), pageable);
            if (followersPage.getContent().isEmpty()) break;

            List<Notification> newNotificationList = followersPage.getContent().stream()
                    .map(follower -> Notification.builder()
                            .notificationEvent(notificationEvent)
                            .userEntity(follower)
                            .isRead(false)
                            .isArchived(false)
                            .build()
                    ).toList();
            List<Notification> savedNoti = notificationRepository.saveAll(newNotificationList);
            for (Notification noti : savedNoti) {
                NotificationDto notificationDto = mapSingleToDto(noti);
                websocketNotificationService.sendPrivateNotification(noti.getUserEntity().getUserId(), notificationDto);
            }
            log.info(">>> batch {} ({} users)", page + 1, newNotificationList.size());
            page++;
        } while (followersPage.hasNext());
        log.info("[WORKER] fan-out notification COMPLETED");

    }

    private NotificationDto mapSingleToDto(Notification n) {
        NotificationEvent e = n.getNotificationEvent();
        UserEntity creator = e.getCreatedBy();

        return NotificationDto.builder()
                .notificationId(n.getId())
                .isRead(n.getIsRead())
                .eventId(e.getEventId())
                .eventName(e.getEventName())
                .eventType(e.getEventType().toString())
                .dateNotice(e.getDateNotice())
                .createdById(creator.getUserId())
                .targetUrl(e.getTargetUrl())
                .createdByName(creator.displayUsername())
                .createdByAvatar(creator.getAvatarUrl())
                .build();
    }
}
