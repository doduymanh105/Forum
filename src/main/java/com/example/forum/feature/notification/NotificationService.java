package com.example.forum.feature.notification;

import com.example.forum.feature.notification.dto.NotificationDto;
import com.example.forum.common.dto.PagedResponse;
import com.example.forum.domain.Enum.EventType;
import com.example.forum.domain.NotificationEvent;
import com.example.forum.domain.UserEntity;

public interface NotificationService {
    NotificationEvent createEvent(EventType eventType, UserEntity creator, String description, Long refereneId, String referenceType);

    void notifyFollowers(NotificationEvent event);

    void notifySpecificUser(UserEntity receiver,NotificationEvent event);

    PagedResponse<NotificationDto> getNotificationsWithReadStatus(int page, int size, String keyword,Boolean isRead);

    Long countUnreadNotifications();

    void markAsRead(Long notificationId);

    void markAllAsRead();

    void archiveNotification(Long notificationId);

    void deleteNotification(Long notificationId);

}
