package com.example.forum.feature.notification.dto;

public record FanoutNotificationMessage(
        Long eventId,
        Long authorId
) {
}
