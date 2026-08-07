package com.example.forum.feature.chat.service;

import java.util.List;
import java.util.Map;

public interface OnlineOfflineService {
    void connect(Long userId, String socketSessionId);
    void disconnect(Long userId, String socketSessionId);
    boolean isOnline(Long userId);
    long getOnlineConnectionCount(Long userId);
    Map<Long, Boolean> checkOnlineStatus(List<Long> userIds);
}
