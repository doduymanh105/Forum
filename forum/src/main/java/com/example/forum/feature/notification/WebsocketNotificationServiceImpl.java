package com.example.forum.feature.notification;

import com.example.forum.common.constant.WebSocketDestination;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebsocketNotificationServiceImpl implements WebsocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendNewMessageNotification(Long chatId, Object messagePayload) {
        String destination = WebSocketDestination.chatMessages(chatId);
        messagingTemplate.convertAndSend(destination, messagePayload);
    }

    @Override
    public void sendReadReceiptNotification(Long chatId, Object receiptPayload) {
        String destination = WebSocketDestination.readReceipt(chatId);
        messagingTemplate.convertAndSend(destination, receiptPayload);
    }

    @Override
    public void sendPrivateNotification(Long userId, Object notificationPayload) {
        String destination = WebSocketDestination.privateNotifications();
        messagingTemplate.convertAndSendToUser(userId.toString(), destination, notificationPayload);
    }

    @Override
    public void sendNewChatNotification(Long userId, Object chatPayload) {
        String destination = WebSocketDestination.newChatNotification();
        messagingTemplate.convertAndSend(destination, chatPayload);
    }

    @Override
    public void sendTypingNotification(Long chatId, Object chatPayload) {
        String destination = WebSocketDestination.typingDestination(chatId);
        messagingTemplate.convertAndSend(destination, chatPayload);
    }


}
