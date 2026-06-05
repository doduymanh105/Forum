package com.example.forum.service;

import com.example.forum.dto.request.chatRequestDto.CreateGroupChatRequest;
import com.example.forum.dto.request.chatRequestDto.UpdateChatRequest;
import com.example.forum.dto.response.chatResponseDto.ChatMessageResponse;
import com.example.forum.dto.response.chatResponseDto.ChatResponse;
import com.example.forum.dto.response.chatResponseDto.CustomPageable;
import org.springframework.web.multipart.MultipartFile;

public interface ChatService {
    CustomPageable<ChatResponse> getChatLists(int page, int size, String sortDir, String sortBy, String keyword);
    ChatResponse getChatDetails(Long id);
    ChatResponse createChat(Long userId);
    ChatResponse createGroupChat(CreateGroupChatRequest request);
    ChatResponse updateChatInfo(UpdateChatRequest request, Long id);
    ChatResponse updateChatAvatar(Long chatId, MultipartFile file);
    void deleteChat(Long id);
    ChatMessageResponse getChatMessage(Long chatId, int page, int size, String keyword);
    ChatMessageResponse sendMediaMessage(Long chatId, MultipartFile file);
}
