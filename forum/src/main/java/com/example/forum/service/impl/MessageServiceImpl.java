package com.example.forum.service.impl;


import com.example.forum.common.utils.SecurityUtils;
import com.example.forum.core.exception.AppException;
import com.example.forum.dto.request.chatRequestDto.EditMessageRequest;
import com.example.forum.dto.response.chatResponseDto.MessageResponse;
import com.example.forum.dto.response.chatResponseDto.SideBarNotificationResponse;
import com.example.forum.dto.response.chatResponseDto.UserResponse;
import com.example.forum.entity.Chat;
import com.example.forum.entity.ChatParticipant;
import com.example.forum.entity.Enum.ErrorCode;
import com.example.forum.entity.Enum.MessageType;
import com.example.forum.entity.Message;
import com.example.forum.entity.UserEntity;
import com.example.forum.repository.ChatParticipantRepository;
import com.example.forum.repository.ChatRepository;
import com.example.forum.repository.MessageRepository;
import com.example.forum.service.ChatNotificationService;
import com.example.forum.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ChatParticipantRepository chatParticipantRepo;
    private final ChatRepository chatRepository;

    private final ChatNotificationService notificationService;
    private final SecurityUtils securityUtils;



    @Override
    @Transactional
    public MessageResponse sendMessage(Long chatId, String content, Long replyId) {
        UserEntity currentUser = securityUtils.getCurrentUser();
        ChatParticipant currentChatParticipant = chatParticipantRepo.findMyParticipantByChatIAndUserId(chatId, currentUser.getUserId())
                .orElseThrow(()-> new AppException(ErrorCode.CHAT_PARTICIPANT_NOT_FOUND));

        if (replyId != null) {
            boolean isValidReply = messageRepository.existsByIdAndChatId(replyId, chatId);
            if (!isValidReply) {
                throw new AppException(ErrorCode.MESSAGE_NOT_FOUND);
            }
        }
        LocalDateTime currentTime = LocalDateTime.now();
        Chat chat = currentChatParticipant.getChat();

        Message message= Message.builder()
                .chat(currentChatParticipant.getChat())
                .sender(currentUser)
                .content(content)
                .type(MessageType.TEXT)
                .replyToMessageId(replyId)
                .sendAt(currentTime)
                .build();

        messageRepository.save(message);

        chat.setLastMessageAt(currentTime);
        chat.setLastMessageContent(content);
        chat.setLastMessageId(message.getId());
        chat.setLastMessageType(MessageType.TEXT);
        chat.setLastSenderName(currentUser.displayUsername());
        chatRepository.save(chat);

        MessageResponse messageResponse = MessageResponse.builder()
                .id(message.getId())
                .sender(UserResponse.mapToUserResponse(currentUser))
                .content(content)
                .type(message.getType())
                .replyToMessageId(message.getReplyToMessageId())
                .isEdited(message.isEdited())
                .sendAt(message.getSendAt())
                .updatedAt(message.getUpdatedAt())
                .build();

        notificationService.sendNewMessageNotification(chatId, messageResponse);

        List<ChatParticipant> chatParticipantList = chatParticipantRepo.findAllByChatId(chatId);
        for(ChatParticipant chatParticipant : chatParticipantList){
            if(chatParticipant.getUserEntity().getUserId().equals(currentUser.getUserId())){
                continue;
            }

            chatParticipant.setUnreadCount(chatParticipant.getUnreadCount()+1);
            chatParticipantRepo.save(chatParticipant);

            SideBarNotificationResponse sidebarPayload = SideBarNotificationResponse.builder()
                    .chatId(chatId)
                    .lastMessage(content)
                    .lastMessageTime(currentTime)
                    .lastMessageType(message.getType())
                    .senderName(currentUser.displayUsername())
                    .unreadCount(chatParticipant.getUnreadCount())
                    .isMute(chatParticipant.isMuted()) // when mute, frontend not notice
                    .build();

            notificationService.sendPrivateNotification(chatParticipant.getUserEntity().getUserId(), sidebarPayload);
        }
        return messageResponse;
    }

    @Override
    @Transactional
    public MessageResponse editMessage(EditMessageRequest request, Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(()-> new AppException(ErrorCode.MESSAGE_NOT_FOUND));

        Long currentUserId = securityUtils.getCurrentUserId();
        if(!currentUserId.equals(message.getSender().getUserId())){
            throw new AppException(ErrorCode.NOT_OWNED_MESSAGE);
        }
        message.setContent(request.getContent());
        message.setEdited(true);
        messageRepository.save(message);
        // TODO: notify sidebar for case other users are in sidebar (async or batch update)
        Chat chat = message.getChat();
        if(message.getId().equals(chat.getLastMessageId())){
            chat.setLastMessageContent(request.getContent());
        }
        MessageResponse response = mapToMessageResponse(message);
        notificationService.sendNewMessageNotification(message.getChat().getId(), response);
        return response;
    }

    public MessageResponse mapToMessageResponse(Message message){
        return MessageResponse.builder()
                .id(message.getId())
                .sender(UserResponse.mapToUserResponse(message.getSender()))
                .content(message.getContent())
                .type(message.getType())
                .replyToMessageId(message.getReplyToMessageId())
                .isEdited(message.isEdited())
                .sendAt(message.getSendAt())
                .updatedAt(message.getUpdatedAt())
                .build();
    }


    @Override
    @Transactional
    public void deleteMessage(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(()-> new AppException(ErrorCode.MESSAGE_NOT_FOUND));

        Long currentUserId = securityUtils.getCurrentUserId();
        if(!currentUserId.equals(message.getSender().getUserId())){
            throw new AppException(ErrorCode.NOT_OWNED_MESSAGE);
        }
        String content= "This message has been recalled";
        message.setDeleted(true);
        message.setContent(content);
        messageRepository.save(message);
        Chat chat = message.getChat();
        if(message.getId().equals(chat.getLastMessageId())){
            chat.setLastMessageContent(content);
        }
        MessageResponse response = mapToMessageResponse(message);
        notificationService.sendNewMessageNotification(message.getChat().getId(), response);
    }
}
