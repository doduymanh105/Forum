package com.example.forum.service.impl;

import com.example.forum.dto.response.chatResponseDto.MessageResponse;
import com.example.forum.dto.response.chatResponseDto.SideBarNotificationResponse;
import com.example.forum.entity.Chat;
import com.example.forum.entity.ChatParticipant;
import com.example.forum.entity.Enum.ChatEvent;
import com.example.forum.entity.Enum.MessageType;
import com.example.forum.entity.Message;
import com.example.forum.entity.UserEntity;
import com.example.forum.repository.ChatParticipantRepository;
import com.example.forum.repository.ChatRepository;
import com.example.forum.repository.MessageRepository;
import com.example.forum.service.ChatEventService;
import com.example.forum.service.ChatNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatEventServiceImpl implements ChatEventService {

    private final MessageRepository messageRepo;
    private final ChatRepository chatRepo;
    private final ChatParticipantRepository chatParticipantRepo;
    private final ChatNotificationService notificationService;


    @Async
    @Override
    @Transactional
    public void processGroupSystemEvent(Chat chat, UserEntity actor, String systemContent, ChatEvent eventType) {
        this.processGroupSystemEvent(chat, actor, systemContent, null, eventType);
    }

    //KICK, CHANGE_ROLE, ADD, LEAVE, CHANGE CHAT INFO/AVATAR
    @Async
    @Override
    @Transactional
    public void processGroupSystemEvent(Chat chat, UserEntity actor, String systemContent, Long targetUserId, ChatEvent eventType) {

        Message systemMessage = Message.builder()
                .chat(chat)
                .sender(actor)
                .content(systemContent)
                .type(MessageType.SYSTEM)
                .sendAt(LocalDateTime.now())
                .build();
        messageRepo.save(systemMessage);

        chat.setLastMessageId(systemMessage.getId());
        chat.setLastMessageContent(systemContent);
        chat.setLastMessageAt(systemMessage.getSendAt());
        chat.setLastSenderName(actor.displayUsername());
        chatRepo.save(chat);

        MessageResponse messageResponse = MessageResponse.mapToMessageResponse(systemMessage);
        notificationService.sendNewMessageNotification(chat.getId(), messageResponse);


        List<ChatParticipant> allParticipants = chatParticipantRepo.findAllByChatId(chat.getId());
        for (ChatParticipant p : allParticipants) {
            Long pId = p.getUserEntity().getUserId();
            if (pId.equals(actor.getUserId())) continue;

            p.setUnreadCount(p.getUnreadCount() + 1);
            chatParticipantRepo.save(p);

            SideBarNotificationResponse sidebarPayload = SideBarNotificationResponse.builder()
                    .chatId(chat.getId())
                    .senderName(actor.displayUsername())
                    .lastMessage(systemContent)
                    .lastMessageTime(systemMessage.getSendAt())
                    .lastMessageType(MessageType.SYSTEM)
                    .eventType(eventType)
                    .build();

            notificationService.sendPrivateNotification(pId, sidebarPayload);
        }


        if (targetUserId != null && (eventType.equals(ChatEvent.KICK) || eventType.equals(ChatEvent.CHANGE_ROLE))) {

            SideBarNotificationResponse targetPayload = SideBarNotificationResponse.builder()
                    .chatId(chat.getId())
                    .lastMessage("New notification from ADMIN")
                    .lastMessageType(MessageType.SYSTEM)
                    .eventType(eventType)
                    .build();

            notificationService.sendPrivateNotification(targetUserId, targetPayload);
        }
    }
}