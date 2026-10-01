package com.example.forum.feature.follow.dto;

import com.example.forum.domain.NotificationEvent;

public record PendingNotification(
        Long targetUserId,
        NotificationEvent notificationEvent
) {}
