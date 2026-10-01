package com.example.forum.feature.notification.dto;

import java.io.Serializable;

public record PrivateNotificationMessage(
        Long eventId,
        Long receiverId
) implements Serializable {}
