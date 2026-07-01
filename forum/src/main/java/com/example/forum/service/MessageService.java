package com.example.forum.service;


import com.example.forum.dto.request.chatRequestDto.EditMessageRequest;
import com.example.forum.dto.response.chatResponseDto.MessageResponse;
import com.example.forum.entity.Chat;
import com.example.forum.entity.ChatParticipant;
import com.example.forum.entity.UserEntity;
import org.springframework.web.multipart.MultipartFile;

public interface MessageService {
    MessageResponse sendMessage(Long chatId, String content, Long replyId);
    MessageResponse editMessage(EditMessageRequest request, Long id);
    void deleteMessage(Long id);
    MessageResponse sendMediaMessage(Long chatId, MultipartFile file);
    MessageResponse saveAndBroadcastMediaMessage(ChatParticipant participant, UserEntity currentUser, String mediaUrl);
    void broadcastToSidebar(Chat chat, UserEntity sender, MessageResponse messageResponse, String sidebarPreviewText, boolean incrementUnread);
}
