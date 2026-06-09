package com.example.forum.service;


import com.example.forum.dto.request.chatRequestDto.EditMessageRequest;
import com.example.forum.dto.response.chatResponseDto.MessageResponse;

public interface MessageService {
    MessageResponse sendMessage(Long chatId, String content, Long replyId);
    MessageResponse editMessage(EditMessageRequest request, Long id);
    void deleteMessage(Long id);
}
