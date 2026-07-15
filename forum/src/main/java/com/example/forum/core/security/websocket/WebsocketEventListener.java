package com.example.forum.core.security.websocket;

import com.example.forum.feature.chat.service.OnlineOfflineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebsocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    private final OnlineOfflineService onlineOfflineService;


    @EventListener
    public void handleWebsocketConnectEvent(SessionConnectedEvent event){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if(accessor.getUser()!= null){
            Long userId = Long.parseLong(accessor.getUser().getName());
            onlineOfflineService.connect(userId, accessor.getSessionId());
            if (onlineOfflineService.getOnlineConnectionCount(userId) == 1) {
                broadcastPresence(userId, true);
            }
            log.info("User Online (Add to Redis): {} | Session: {}", userId, accessor.getSessionId());
        }
    }

    @EventListener
    public void handleWebsocketDisconnectEvent(SessionDisconnectEvent event){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getUser() != null) {
            Long userId = Long.parseLong(accessor.getUser().getName());

            onlineOfflineService.disconnect(userId, accessor.getSessionId());

            if (!onlineOfflineService.isOnline(userId)) {
                broadcastPresence(userId, false);
                log.info("User Offline (Removed from Redis): {}", userId);
            } else {
                log.info("User {} closed a tab, but still has other active sessions.", userId);
            }
        }
    }

    private void broadcastPresence(Long userId, boolean isOnline) {
        messagingTemplate.convertAndSend("/topic/public/presence",
                Map.of("userId", userId, "isOnline", isOnline));
    }
}
