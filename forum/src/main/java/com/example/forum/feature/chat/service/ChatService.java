package com.example.forum.feature.chat.service;

import com.example.forum.feature.chat.dto.chatRequestDto.CreateGroupChatRequest;
import com.example.forum.feature.chat.dto.chatRequestDto.UpdateChatRequest;
import com.example.forum.feature.chat.dto.chatResponseDto.ChatMessageResponse;
import com.example.forum.feature.chat.dto.chatResponseDto.ChatResponse;
import com.example.forum.feature.chat.dto.chatResponseDto.CustomPageable;
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
}
