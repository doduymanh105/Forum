package com.example.forum.feature.chat.service.impl;

import com.example.forum.common.constant.AppConstants;
import com.example.forum.common.service.cache.CacheService;
import com.example.forum.feature.chat.service.OnlineOfflineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OnlineOfflineServiceImpl implements OnlineOfflineService {

    private final CacheService cacheService;


    private String getOnlineKey(Long userId) {
        return AppConstants.ONLINE_KEY_PREFIX + userId;
    }

    @Override
    public void connect(Long userId, String socketSessionId) {
        cacheService.addToSet(
                getOnlineKey(userId),
                socketSessionId
        );
    }

    @Override
    public void disconnect(Long userId, String socketSessionId) {
        cacheService.removeFromSet(
                getOnlineKey(userId),
                socketSessionId
        );

        /*
         * Không cần delete key khi Set rỗng.
         * Redis tự xóa key khi SREM xóa member cuối cùng.
         */
    }
    @Override
    public boolean isOnline(Long userId) {
        return cacheService.hasKey(getOnlineKey(userId));
    }

    @Override
    public long getOnlineConnectionCount(Long userId) {
        return cacheService.getSetSize(getOnlineKey(userId));
    }

    @Override
    public Map<Long, Boolean> checkOnlineStatus(List<Long> userIds) {
        Map<Long, Boolean> result = new HashMap<>();
        for (Long userId : userIds) {
            result.put(userId, isOnline(userId));
        }
        return result;
    }

}
