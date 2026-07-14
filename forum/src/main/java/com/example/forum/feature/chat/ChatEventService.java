package com.example.forum.feature.chat;


import com.example.forum.domain.Chat;
import com.example.forum.domain.Enum.ChatEvent;
import com.example.forum.domain.UserEntity;

public interface ChatEventService {
    void processGroupSystemEvent(
            Chat chat,
            UserEntity actor,
            String systemContent,
            ChatEvent eventType
    );

    void processGroupSystemEvent(
            Chat chat,
            UserEntity actor,
            String systemContent,
            Long targetUserId,
            ChatEvent eventType
    );
}
