package com.example.forum.service.impl;


import com.example.forum.common.utils.SecurityUtils;
import com.example.forum.core.exception.AppException;
import com.example.forum.dto.request.chatRequestDto.CreateGroupChatRequest;
import com.example.forum.dto.request.chatRequestDto.UpdateChatRequest;
import com.example.forum.dto.response.chatResponseDto.*;
import com.example.forum.entity.*;
import com.example.forum.entity.Enum.ChatRole;
import com.example.forum.entity.Enum.ErrorCode;
import com.example.forum.repository.ChatParticipantRepository;
import com.example.forum.repository.ChatRepository;
import com.example.forum.repository.MessageRepository;
import com.example.forum.repository.UserRepository;
import com.example.forum.service.ChatNotificationService;
import com.example.forum.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepo;
    private final ChatParticipantRepository chatParticipantRepo;
    private final UserRepository userRepo;
    private final MessageRepository messageRepo;

    private final ChatNotificationService notificationService;
    private final SecurityUtils securityUtils;

    @Override
    public CustomPageable<ChatResponse> getChatLists(int page, int size, String sortDir, String sortBy, String keyword) {
        int pageNumber = page > 0 ? page - 1 : 0;

        Sort.Direction sortDirect;
        if(sortDir.equalsIgnoreCase("asc")){
            sortDirect= Sort.Direction.ASC;
        } else {
            sortDirect = Sort.Direction.DESC;
        }
        if(sortBy == null){
            sortBy= "createdAt";
        }
        if (keyword == null){
            keyword="";
        }
        Sort sort = Sort.by(sortDirect, sortBy);
        Pageable pageable = PageRequest.of(pageNumber, size, sort);

        Long currentUserId = securityUtils.getCurrentUserId();
        Page<ChatResponse> chatPage = chatParticipantRepo.findAllByUserId(currentUserId,keyword,pageable);
        List<ChatResponse> chatResponses = chatPage.getContent();

        return CustomPageable.<ChatResponse>builder()
                .pageSize(chatPage.getSize())
                .totalElements(chatPage.getTotalElements())
                .currentPage(chatPage.getNumber()+1)
                .totalPages(chatPage.getTotalPages())
                .data(chatResponses)
                .build();
    }


    @Override
    public ChatResponse getChatDetails(Long id) {
        Long currentUserId = securityUtils.getCurrentUserId();
        ChatParticipant chatParticipant = findChatParticipant(currentUserId, id);

        return mapToChatResponse(chatParticipant);
    }

    public ChatParticipant findChatParticipant(Long userId1, Long userId2){
        Optional<ChatParticipant> chatParticipant = chatParticipantRepo.findByUserIdAndChatId(userId1, userId2);
        if (chatParticipant.isEmpty()){
            throw new AppException(ErrorCode.CHAT_PARTICIPANT_NOT_FOUND);
        }
        return chatParticipant.get();
    }
    public ChatResponse mapToChatResponse(ChatParticipant cp){
        Chat chat = cp.getChat();
        return ChatResponse.builder()
                .chatId(chat.getId())
                .chatName(chat.getChatName())
                .chatAvatarUrl(chat.getChatAvatarUrl())
                .isGroup(chat.isGroup())
                .createdAt(chat.getCreatedAt())
                .isPinned(cp.isPinned())
                .isMuted(cp.isMuted())
                .isArchived(cp.isArchived())
                .build();
    }

    @Override
    @Transactional
    public ChatResponse createChat(Long userId) {

        Long currentUserId = securityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        if (currentUserId.equals(userId)) {
            throw new AppException(ErrorCode.SELF_CHAT_CREATE);
        }
        Optional<Chat> existingChat = chatParticipantRepo.findPrivateChatBetween(currentUserId, userId);
        if (existingChat.isPresent()) {
            return getChatDetails(existingChat.get().getId());
        }

        List<UserEntity> userList = userRepo.findAllById(List.of(currentUserId, userId));
        if (userList.size() < 2) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        Chat chat = new Chat();
        chat.setGroup(false);
        Chat savedChat= chatRepo.save(chat);

        List<ChatParticipant> chatParticipantList = createChatParticipants(userList,savedChat, currentUserId);
        List<ChatParticipant> savedParticipants= chatParticipantRepo.saveAll(chatParticipantList);

        notifyAllChatParticipant(savedParticipants);

        ChatParticipant currentUserParticipant = savedParticipants.stream()
                .filter(cp -> cp.getUserEntity().getUserId().equals(currentUserId))
                .findFirst()
                .orElse(chatParticipantList.get(0));

        return mapToChatResponse(currentUserParticipant);
    }

    public List<ChatParticipant> createChatParticipants(List<UserEntity> userList, Chat chat, Long creatorId){
        List<ChatParticipant> chatParticipants = new ArrayList<>();
        for(UserEntity user : userList){
            ChatParticipantKey key = new ChatParticipantKey(chat.getId(), user.getUserId());
            ChatRole assignedRole = user.getUserId().equals(creatorId) ? ChatRole.ADMIN : ChatRole.MEMBER;
            ChatParticipant chatParticipant = ChatParticipant.builder()
                    .id(key)
                    .chat(chat)
                    .userEntity(user)
                    .role(assignedRole)
                    .addedBy(creatorId)
                    .build();
            chatParticipants.add(chatParticipant);
        }
        return chatParticipants;
    }


    @Override
    public ChatResponse createGroupChat(CreateGroupChatRequest request) {
        // check friend
        UserEntity currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        List<UserEntity> userList = userRepo.findAllById(request.getUserIdList());
        userList.add(currentUser);
        if (userList.size() < 2) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        Chat groupChat = new Chat();
        groupChat.setGroup(true);
        Chat savedChat= chatRepo.save(groupChat);

        List<ChatParticipant> participants = createChatParticipants(userList, groupChat, currentUser.getUserId());
        List<ChatParticipant> savedChatParticipant = chatParticipantRepo.saveAll(participants);

        notifyAllChatParticipant(savedChatParticipant);

        ChatParticipant creatorChatParticipant = savedChatParticipant.stream()
                .filter(chatParticipant ->
                        chatParticipant.getUserEntity().getUserId().equals(currentUser.getUserId())
                ).findFirst()
                .orElse(savedChatParticipant.get(0));

        return mapToChatResponse(creatorChatParticipant);
    }

    @Override
    public ChatResponse updateChatInfo(UpdateChatRequest request, Long chatId) {
        Chat chat = chatRepo.findByIdAndIsDeletedFalse(chatId)
                .orElseThrow(()-> new AppException(ErrorCode.CHAT_NOT_FOUND));
        Long currentUserId = securityUtils.getCurrentUserId();
        ChatParticipant chatParticipant = chatParticipantRepo.findMyParticipantByChatIAndUserId(chatId, currentUserId)
                .orElseThrow(()-> new AppException(ErrorCode.CHAT_PARTICIPANT_NOT_FOUND));
        if(!ChatRole.ADMIN.equals(chatParticipant.getRole())){
            throw new AppException(ErrorCode.CHAT_ACTION_FORBIDDEN);
        }

        chat.setChatName(request.getChatName());
        chatRepo.save(chat);

        List<ChatParticipant> chatParticipants = chatParticipantRepo.findAllByChatId(chatId);
        notifyAllChatParticipant(chatParticipants);
        return mapToChatResponse(chatParticipant);
    }

    public void notifyAllChatParticipant(List<ChatParticipant> participants){
        UserEntity currentUser = securityUtils.getCurrentUser();
        for (ChatParticipant participant : participants) {
            if (participant.getUserEntity().getUserId().equals(currentUser.getUserId())) {
                continue;
            }
            ChatResponse notificationPayload = mapToChatResponse(participant);
            notificationService.sendNewChatNotification(participant.getUserEntity().getUserId(), notificationPayload);
        }
    }

    @Override
    public ChatResponse updateChatAvatar(Long chatId, MultipartFile file) {
        // when have upload module
        return null;
    }

    @Override
    public void deleteChat(Long chatId) {
        Chat chat = chatRepo.findByIdAndIsDeletedFalse(chatId)
                .orElseThrow(()-> new AppException(ErrorCode.CHAT_NOT_FOUND));
        Long currentUserId = securityUtils.getCurrentUserId();
        ChatParticipant chatParticipant = chatParticipantRepo.findMyParticipantByChatIAndUserId(chatId, currentUserId)
                .orElseThrow(()-> new AppException(ErrorCode.CHAT_PARTICIPANT_NOT_FOUND));
        if(!ChatRole.ADMIN.equals(chatParticipant.getRole())){
            throw new AppException(ErrorCode.CHAT_ACTION_FORBIDDEN);
        }
        chat.setDeleted(true);
        chatRepo.save(chat);
    }

    @Override
    public ChatMessageResponse getChatMessage(Long chatId, int page, int size, String keyword) {
        Chat chat = chatRepo.findByIdAndIsDeletedFalse(chatId)
                .orElseThrow(()-> new AppException(ErrorCode.CHAT_NOT_FOUND));

        Long currentUserId = securityUtils.getCurrentUserId();



        int pageNumber = page> 0 ? page -1: 0;
        Sort.Direction sortDir = Sort.Direction.DESC;
        Sort sort = Sort.by(sortDir, "sendAt");
        if(keyword == null){
            keyword="";
        }
        Pageable pageable = PageRequest.of(pageNumber, size, sort);
        List<MemberResponse> chatParticipants = chatParticipantRepo.findAllMemberByChatId(chatId);

        MemberResponse myChatParticipant = chatParticipants.stream().filter(
                chatP -> chatP.getUserId().equals(currentUserId)
        ).findFirst().orElse(null);
        if(myChatParticipant == null){
            throw new AppException(ErrorCode.CHAT_PARTICIPANT_NOT_FOUND);
        }

        Page<Message> messagePage= messageRepo.findByChatId(chatId, keyword, pageable);
        Page<MessageResponse> messageResponses = messagePage.map(
                msg -> {
                    Long senderId = msg.getSender() != null ? msg.getSender().getUserId() : null;
                    return MessageResponse.mapToMessageResponse(msg);
                }
        );

        return ChatMessageResponse.builder()
                .chat(mapToChatOverview(chat))
                .activeParticipants(chatParticipants)
                .messages(CustomPageable.mapToCustomPageable(messageResponses))
                .build();
    }

    @Override
    public ChatMessageResponse sendMediaMessage(Long chatId, MultipartFile file) {
        // when have upload module
        return null;
    }

    public ChatOverview mapToChatOverview(Chat chat){
        return ChatOverview.builder()
                .id(chat.getId())
                .chatName(chat.getChatName())
                .chatAvatarUrl(chat.getChatAvatarUrl())
                .isGroup(chat.isGroup())
                .createdAt(chat.getCreatedAt())
                .build();
    }

}
