package com.example.forum.service.impl;


import com.example.forum.common.utils.SecurityUtils;
import com.example.forum.core.exception.AppException;
import com.example.forum.dto.request.chatRequestDto.EditMessageRequest;
import com.example.forum.dto.response.UploadResponseDto;
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
import com.example.forum.service.CloudinaryService;
import com.example.forum.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ChatParticipantRepository chatParticipantRepo;
    private final ChatRepository chatRepository;

    private final ChatNotificationService notificationService;
    private final CloudinaryService cloudinaryService;
    private final SecurityUtils securityUtils;

    @org.springframework.context.annotation.Lazy
    @org.springframework.beans.factory.annotation.Autowired
    private MessageService self;



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
        self.broadcastToSidebar(chat, currentUser, messageResponse, content, true);
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
        boolean isLastMessage = message.getId().equals(chat.getLastMessageId());
        if(isLastMessage){
            chat.setLastMessageContent(request.getContent());
        }
        MessageResponse response = mapToMessageResponse(message);
        if (isLastMessage) {
            self.broadcastToSidebar(chat, message.getSender(), response, request.getContent(), false);
        }
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

        UserEntity currentUser = securityUtils.getCurrentUser();
        if(!currentUser.getUserId().equals(message.getSender().getUserId())){
            throw new AppException(ErrorCode.NOT_OWNED_MESSAGE);
        }
        String content= "This message has been recalled";
        message.setDeleted(true);
        message.setContent(content);
        message = messageRepository.save(message);
        MessageResponse response = mapToMessageResponse(message);
        Chat chat = message.getChat();
        if(message.getId().equals(chat.getLastMessageId())){
            chat.setLastMessageContent(content);
            chatRepository.save(chat);
            self.broadcastToSidebar(chat, currentUser, response, content, false);
        }

        notificationService.sendNewMessageNotification(message.getChat().getId(), response);
    }

    @Override
    public MessageResponse sendMediaMessage(Long chatId, MultipartFile file) {
        UserEntity currentUser = securityUtils.getCurrentUser();
        ChatParticipant currentChatParticipant = chatParticipantRepo.findMyParticipantByChatIAndUserId(chatId, currentUser.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.CHAT_PARTICIPANT_NOT_FOUND));

        UploadResponseDto uploadResult = cloudinaryService.uploadImage(file);
        String mediaUrl = uploadResult.getUrl();
        // when have upload module
        return self.saveAndBroadcastMediaMessage(currentChatParticipant, currentUser, mediaUrl);
    }

    @Override
    @Transactional
    public MessageResponse saveAndBroadcastMediaMessage(ChatParticipant participant, UserEntity currentUser, String mediaUrl) {
        Chat chat = participant.getChat();
        LocalDateTime currentTime = LocalDateTime.now();

        Message message = Message.builder()
                .chat(chat)
                .sender(currentUser)
                .content(mediaUrl)
                .type(MessageType.IMAGE)
                .sendAt(currentTime)
                .build();
        messageRepository.save(message);

        chat.setLastMessageAt(currentTime);
        chat.setLastMessageContent("Image");
        chat.setLastMessageId(message.getId());
        chat.setLastMessageType(MessageType.IMAGE);
        chat.setLastSenderName(currentUser.displayUsername());
        chatRepository.save(chat);

        MessageResponse messageResponse = MessageResponse.mapToMessageResponse(message);
        notificationService.sendNewMessageNotification(chat.getId(), messageResponse);

        self.broadcastToSidebar(chat, currentUser, messageResponse, "Image", true);
        return  messageResponse;
    }

    @Override
    @Async
    @Transactional
    public void broadcastToSidebar(Chat chat, UserEntity sender, MessageResponse messageResponse, String sidebarPreviewText, boolean incrementUnread) {

        List<ChatParticipant> chatParticipantList = chatParticipantRepo.findAllByChatId(chat.getId());

        for (ChatParticipant cp : chatParticipantList) {
            if (cp.getUserEntity().getUserId().equals(sender.getUserId())) {
                continue;
            }
            // for case: new message
            if (incrementUnread) {
                cp.setUnreadCount(cp.getUnreadCount() + 1);
                chatParticipantRepo.save(cp);
            }

            SideBarNotificationResponse sidebarPayload = SideBarNotificationResponse.builder()
                    .chatId(chat.getId())
                    .lastMessage(sidebarPreviewText)
                    .lastMessageTime(messageResponse.getSendAt())
                    .lastMessageType(messageResponse.getType())
                    .senderName(sender.displayUsername())
                    .unreadCount(cp.getUnreadCount())
                    .isMute(cp.isMuted())
                    .build();

            notificationService.sendPrivateNotification(cp.getUserEntity().getUserId(), sidebarPayload);
        }
    }
}
