package com.example.forum.common.constant;

public class WebSocketDestination {

    private static final String TOPIC_CHAT_PREFIX = "/topic/chats/";
    private static final String USER_QUEUE_PREFIX = "/queue/";

    public static String chatMessages(Long chatId) {
        return TOPIC_CHAT_PREFIX + chatId + "/messages";
    }

    public static String readReceipt(Long chatId) {
        return TOPIC_CHAT_PREFIX + chatId + "/read-receipt";
    }

    public static String privateNotifications() {
        return USER_QUEUE_PREFIX + "notifications";
    }

    public static String newChatNotification() {
        return "/queue/new-chat"; // Ví dụ: /user/queue/new-chat
    }
}
