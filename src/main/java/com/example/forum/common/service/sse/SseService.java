package com.example.forum.common.service.sse;

import com.example.forum.feature.notification.dto.NotificationDto;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseService {
    SseEmitter subscribe(Long userId);
    void sendRealTimeEvent(Long recipientId, NotificationDto notificationData);

}
