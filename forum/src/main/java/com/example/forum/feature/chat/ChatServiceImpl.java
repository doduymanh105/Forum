package com.example.forum.feature.chat;


import com.example.forum.common.constant.MessageConstants;
import com.example.forum.common.service.cache.RedisService;
import com.example.forum.common.utils.SecurityUtils;
import com.example.forum.core.exception.AppException;
import com.example.forum.feature.chat.dto.chatRequestDto.CreateGroupChatRequest;
import com.example.forum.feature.chat.dto.chatRequestDto.UpdateChatRequest;
import com.example.forum.feature.media.dto.UploadResponseDto;
import com.example.forum.domain.*;
import com.example.forum.domain.Enum.ChatEvent;
import com.example.forum.domain.Enum.ChatRole;
import com.example.forum.core.exception.ErrorCode;
import com.example.forum.feature.chat.dto.chatResponseDto.*;
import com.example.forum.feature.user.UserRepository;
import com.example.forum.feature.notification.chatNoti.ChatNotificationService;
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
    private final RedisService.CloudinaryService cloudinaryService;
    private final SecurityUtils securityUtils;
    private final ChatEventService chatEventService;

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

    public ChatParticipant findChatParticipant(Long userId1, Long chatId){
        Optional<ChatParticipant> chatParticipant = chatParticipantRepo.findByUserIdAndChatId(userId1, chatId);
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
        //TODO: send invitation and wait for acceptance

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
    @Transactional
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
    @Transactional
    public ChatResponse updateChatInfo(UpdateChatRequest request, Long chatId) {
        Chat chat = chatRepo.findByIdAndIsDeletedFalse(chatId)
                .orElseThrow(()-> new AppException(ErrorCode.CHAT_NOT_FOUND));
        UserEntity currentUser = securityUtils.getCurrentUser();
        ChatParticipant chatParticipant = chatParticipantRepo.findMyParticipantByChatIAndUserId(chatId, currentUser.getUserId())
                .orElseThrow(()-> new AppException(ErrorCode.CHAT_PARTICIPANT_NOT_FOUND));
        if(!ChatRole.ADMIN.equals(chatParticipant.getRole())){
            throw new AppException(ErrorCode.CHAT_ACTION_FORBIDDEN);
        }

        chat.setChatName(request.getChatName());
        chat = chatRepo.save(chat);

        String message = MessageConstants.changeGroupName(currentUser, chat.getChatName());
        chatEventService.processGroupSystemEvent(chat, currentUser,message, ChatEvent.CHANGE_NAME );
        return mapToChatResponse(chatParticipant);
    }

    public void notifyAllChatParticipant(List<ChatParticipant> chatParticipants){
        UserEntity currentUser = securityUtils.getCurrentUser();
        for (ChatParticipant participant : chatParticipants) {
            if (participant.getUserEntity().getUserId().equals(currentUser.getUserId())) {
                continue;
            }
            ChatResponse notificationPayload = mapToChatResponse(participant);
            notificationService.sendNewChatNotification(participant.getUserEntity().getUserId(), notificationPayload);
        }
    }

    @Override
    public ChatResponse updateChatAvatar(Long chatId, MultipartFile file) {
        UserEntity user = securityUtils.getCurrentUser();
        ChatParticipant chatParticipant = chatParticipantRepo.findMyParticipantByChatIAndUserId(chatId, user.getUserId())
                .orElseThrow(()-> new AppException(ErrorCode.CHAT_PARTICIPANT_NOT_FOUND));

        Chat chat = chatParticipant.getChat();
        if(!chat.isGroup()){
            throw new AppException(ErrorCode.GROUP_CHAT_FEATURE);
        }
        UploadResponseDto uploadImage = cloudinaryService.uploadImage(file);
        chat.setChatAvatarUrl(uploadImage.getUrl());
        chatRepo.save(chat);
        //TODO: notification to group chat
        String message = MessageConstants.changeGroupChatAvatar(user);
        chatEventService.processGroupSystemEvent(chat, user,message, ChatEvent.CHANGE_AVATAR );
        return mapToChatResponse(chatParticipant);
    }

    @Override
    @Transactional
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
