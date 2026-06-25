package com.example.forum.service;

public interface ChatNotificationService {
    void sendNewMessageNotification(Long chatId, Object messagePayload);
    void sendReadReceiptNotification(Long chatId, Object receiptPayload);
    void sendPrivateNotification(Long userId, Object notificationPayload);
    void sendNewChatNotification(Long userId,Object chatPayload);
    void sendTypingNotification(Long chatId, Object chatPayload);
}
