package com.example.forum.service;


import com.example.forum.entity.Chat;
import com.example.forum.entity.Enum.ChatEvent;
import com.example.forum.entity.UserEntity;

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
