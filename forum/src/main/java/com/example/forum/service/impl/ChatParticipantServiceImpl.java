package com.example.forum.service.impl;

import com.example.forum.common.constant.MessageConstants;
import com.example.forum.common.utils.SecurityUtils;
import com.example.forum.core.exception.AppException;
import com.example.forum.dto.request.chatRequestDto.ChangeRoleRequest;
import com.example.forum.dto.request.chatRequestDto.UpdateChatSettingRequest;
import com.example.forum.dto.response.chatResponseDto.MemberResponse;
import com.example.forum.dto.response.chatResponseDto.MyChatSettingResponse;
import com.example.forum.dto.response.chatResponseDto.ReadReceiptResponse;
import com.example.forum.entity.*;
import com.example.forum.entity.Enum.ChatEvent;
import com.example.forum.entity.Enum.ChatRole;
import com.example.forum.entity.Enum.ErrorCode;
import com.example.forum.repository.ChatParticipantRepository;
import com.example.forum.repository.ChatRepository;
import com.example.forum.repository.MessageRepository;
import com.example.forum.repository.UserRepository;
import com.example.forum.service.ChatEventService;
import com.example.forum.service.ChatNotificationService;
import com.example.forum.service.ChatParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatParticipantServiceImpl implements ChatParticipantService {

    private final ChatParticipantRepository chatParticipantRepo;
    private final ChatRepository chatRepo;
    private final UserRepository userRepo;
    private final MessageRepository messageRepo;

    private final ChatNotificationService notificationService;
    private final ChatEventService chatEventService;
    private final SecurityUtils securityUtils;

    @Override
    public List<MemberResponse> getMemberFromChat(Long chatId) {
        Chat chat = chatRepo.findByIdAndIsDeletedFalse(chatId)
                .orElseThrow(()->new AppException(ErrorCode.CHAT_NOT_FOUND));
        return chatParticipantRepo.findAllMemberByChatId(chatId);
    }

    @Override
    @Transactional
    public List<MemberResponse> addNewMemberToChat(Long chatId, List<Long> memberIdList) {
        //Future extend: put in a pending for group's admin to accept
        Chat chat = chatRepo.findByIdAndIsDeletedFalse(chatId)
                .orElseThrow(()->new AppException(ErrorCode.CHAT_NOT_FOUND));
        UserEntity currentUser= securityUtils.getCurrentUser();
        // check-group's size
        List<Long> existingUserIds = chatParticipantRepo.findAllUserIdsByChatId(chatId);

        List<Long> newUserIds = memberIdList.stream()
                .filter(id -> !existingUserIds.contains(id))
                .toList();
        if (newUserIds.isEmpty()) return new ArrayList<>();

        List<UserEntity> userList = userRepo.findAllById(newUserIds);
        List<ChatParticipant> newAddedMember = createChatParticipants(userList, chat,currentUser.getUserId());
        chatParticipantRepo.saveAll(newAddedMember);


        String systemContent = MessageConstants.addMemberMessage(currentUser, userList);

        // notify inside chat
        // notify new participant about participate new chat
        // notify sidebar ++ unReadCount
        chatEventService.processGroupSystemEvent(chat, currentUser, systemContent, ChatEvent.ADD);

        return  mapToMemberResponse(newAddedMember);
    }

    public List<MemberResponse> mapToMemberResponse(List<ChatParticipant> chatParticipants){
        return chatParticipants.stream().map(
                participant -> MemberResponse.builder()
                        .userId(participant.getUserEntity().getUserId())
                        .fullName(participant.getUserEntity().displayUsername())
                        .avatarUrl(participant.getUserEntity().getAvatarUrl())
                        .role(participant.getRole())
                        .lastReadMessageId(participant.getLastReadMessageId())
                        .createdAt(participant.getCreatedAt())
                        .build()
        ).toList();
    }
    public MemberResponse mapToSingleMemberResponse(ChatParticipant participant){
        return  MemberResponse.builder()
                        .userId(participant.getUserEntity().getUserId())
                        .fullName(participant.getUserEntity().displayUsername())
                        .avatarUrl(participant.getUserEntity().getAvatarUrl())
                        .role(participant.getRole())
                        .lastReadMessageId(participant.getLastReadMessageId())
                        .createdAt(participant.getCreatedAt())
                        .build();
    }
    public List<ChatParticipant> createChatParticipants(List<UserEntity> userList, Chat chat, Long requestedId){
        List<ChatParticipant> chatParticipants = new ArrayList<>();
        for(UserEntity user : userList){
            ChatParticipantKey key = new ChatParticipantKey(chat.getId(), user.getUserId());
            ChatParticipant chatParticipant = ChatParticipant.builder()
                    .id(key)
                    .chat(chat)
                    .userEntity(user)
                    .role(ChatRole.MEMBER)
                    .addedBy(requestedId)
                    .build();
            chatParticipants.add(chatParticipant);
        }
        return chatParticipants;
    }

    @Override
    @Transactional
    public MemberResponse changeMemberRole(Long chatId, Long memberId, ChangeRoleRequest request) {

        UserEntity actor = securityUtils.getCurrentUser();
        ChatParticipant currentUserParticipant = chatParticipantRepo.findMyParticipantByChatIAndUserId(chatId, actor.getUserId())
                .orElseThrow(()-> new AppException(ErrorCode.CHAT_PARTICIPANT_NOT_FOUND));

        if(!currentUserParticipant.getRole().equals(ChatRole.ADMIN)){
            throw new AppException(ErrorCode.CHAT_ACTION_FORBIDDEN);
        }
        // tối ưu hơn, gộp lấy 2 chatParticipant 1 lúc
        ChatParticipant targetMember = chatParticipantRepo.findMyParticipantByChatIAndUserId(chatId, memberId)
                .orElseThrow(()-> new AppException(ErrorCode.CHAT_PARTICIPANT_NOT_FOUND));

        if (ChatRole.ADMIN.equals(targetMember.getRole()) && ChatRole.MEMBER.equals(request.getRole())) {
            long adminCount = chatParticipantRepo.countAdminByChatId(chatId);
            if (adminCount <= 1) {
                throw new AppException(ErrorCode.CHAT_MUST_HAVE_ADMIN);
            }
        }
        targetMember.setRole(request.getRole());
        chatParticipantRepo.save(targetMember);

        String systemContent = MessageConstants.roleUpdateMessage(actor,targetMember.getUserEntity(), request.getRole());
        chatEventService.processGroupSystemEvent(
                currentUserParticipant.getChat(),
                currentUserParticipant.getUserEntity(),
                systemContent,
                memberId,
                ChatEvent.CHANGE_ROLE
                );

        return mapToSingleMemberResponse(targetMember);
    }



    @Override
    @Transactional
    public ReadReceiptResponse readNewestChatMessage(Long chatId) {
        Long currentUserId = securityUtils.getCurrentUserId();
        ChatParticipant currentUserParticipant = chatParticipantRepo.findMyParticipantByChatIAndUserId(chatId, currentUserId)
                .orElseThrow(()-> new AppException(ErrorCode.CHAT_PARTICIPANT_NOT_FOUND));
        Message newestMessage = messageRepo.findTopByChatIdOrderBySendAtDesc(chatId)
                .orElseThrow(()-> new AppException(ErrorCode.MESSAGE_NOT_FOUND));
        currentUserParticipant.setLastReadMessageId(newestMessage.getId());
        currentUserParticipant.setUnreadCount(0);
        chatParticipantRepo.save(currentUserParticipant);

        ReadReceiptResponse response = ReadReceiptResponse.builder()
                .chatId(chatId)
                .userId(currentUserId)
                .lastReadMessageId(currentUserParticipant.getLastReadMessageId())
                .unreadCount(currentUserParticipant.getUnreadCount())
                .build();

        notificationService.sendReadReceiptNotification(chatId, response);

        return response;
    }

    @Override
    @Transactional
    public MyChatSettingResponse updateChatSetting(Long chatId, UpdateChatSettingRequest request) {
        ChatParticipant currentUserParticipant = chatParticipantRepo.findMyParticipantByChatIAndUserId(chatId, securityUtils.getCurrentUserId())
                .orElseThrow(()-> new AppException(ErrorCode.CHAT_PARTICIPANT_NOT_FOUND));
        if (request.getIsMuted() != null) {
            currentUserParticipant.setMuted(request.getIsMuted());
        }
        if (request.getIsPinned() != null) {
            currentUserParticipant.setPinned(request.getIsPinned());
        }
        if (request.getIsArchived() != null) {
            if (request.getIsArchived()) {
                 currentUserParticipant.setPinned(false);
            }
            currentUserParticipant.setArchived(request.getIsArchived());
        }

        chatParticipantRepo.save(currentUserParticipant);
        return MyChatSettingResponse.builder()
                .isMuted(currentUserParticipant.isMuted())
                .isPinned(currentUserParticipant.isPinned())
                .isArchived(currentUserParticipant.isArchived())
                .unreadCount(currentUserParticipant.getUnreadCount())
                .build();
    }

    @Override
    @Transactional
    public void removeMemberFromChat(Long chatId, Long memberId) {
        Long currentUserId =securityUtils.getCurrentUserId();
        ChatParticipant currentUserParticipant = chatParticipantRepo
                .findMyParticipantByChatIAndUserId(chatId, currentUserId)
                .orElseThrow(()-> new AppException(ErrorCode.CHAT_PARTICIPANT_NOT_FOUND));
        if(currentUserParticipant.getRole()!= ChatRole.ADMIN){
            throw new AppException(ErrorCode.CHAT_ACTION_FORBIDDEN);
        }
        ChatParticipant targetUserParticipant = chatParticipantRepo.
                findMyParticipantByChatIAndUserId(chatId, memberId)
                .orElseThrow(()-> new AppException(ErrorCode.CHAT_PARTICIPANT_NOT_FOUND));
        targetUserParticipant.setDeleted(true);
        targetUserParticipant.setDeletedBy(currentUserId);
        chatParticipantRepo.save(targetUserParticipant);

        String systemContent = MessageConstants.kickMessage(currentUserParticipant.getUserEntity(), targetUserParticipant.getUserEntity());
        chatEventService.processGroupSystemEvent(
                currentUserParticipant.getChat(),
                currentUserParticipant.getUserEntity(),
                systemContent,
                targetUserParticipant.getUserEntity().getUserId(),
                ChatEvent.KICK
                );
    }

    @Override
    @Transactional
    public void leaveChat(Long chatId) {
        Long currentUserId =securityUtils.getCurrentUserId();
        ChatParticipant currentUserParticipant = chatParticipantRepo.
                findMyParticipantByChatIAndUserId(chatId, currentUserId)
                .orElseThrow(()-> new AppException(ErrorCode.CHAT_PARTICIPANT_NOT_FOUND));
        if (ChatRole.ADMIN.equals(currentUserParticipant.getRole())) {
            long adminCount = chatParticipantRepo.countAdminByChatId(chatId);
            if(adminCount <= 1){
                throw new AppException(ErrorCode.LAST_ADMIN_CANNOT_LEAVE);
            }
        }
        currentUserParticipant.setDeleted(true);
        currentUserParticipant.setDeletedBy(currentUserId);
        chatParticipantRepo.save(currentUserParticipant);

        UserEntity actor = currentUserParticipant.getUserEntity();
        String systemContent = MessageConstants.leaveChatMessage(actor);

        chatEventService.processGroupSystemEvent(currentUserParticipant.getChat(), actor, systemContent, ChatEvent.LEAVE);
    }
}
