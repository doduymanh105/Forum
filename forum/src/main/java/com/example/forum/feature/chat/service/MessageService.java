package com.example.forum.feature.chat.service;


import com.example.forum.feature.chat.dto.chatRequestDto.EditMessageRequest;
import com.example.forum.feature.chat.dto.chatResponseDto.MessageResponse;
import com.example.forum.domain.Chat;
import com.example.forum.domain.ChatParticipant;
import com.example.forum.domain.UserEntity;
import org.springframework.web.multipart.MultipartFile;

public interface MessageService {
    MessageResponse sendMessage(Long chatId, String content, Long replyId);
    MessageResponse editMessage(EditMessageRequest request, Long id);
    void deleteMessage(Long id);
    MessageResponse sendMediaMessage(Long chatId, MultipartFile file);
    MessageResponse saveAndBroadcastMediaMessage(ChatParticipant participant, UserEntity currentUser, String mediaUrl);
    void broadcastToSidebar(Chat chat, UserEntity sender, MessageResponse messageResponse, String sidebarPreviewText, boolean incrementUnread);
}
