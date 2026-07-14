package com.example.forum.feature.chat;



import com.example.forum.feature.chat.dto.chatRequestDto.ChangeRoleRequest;
import com.example.forum.feature.chat.dto.chatRequestDto.UpdateChatSettingRequest;
import com.example.forum.feature.chat.dto.chatResponseDto.MemberResponse;
import com.example.forum.feature.chat.dto.chatResponseDto.MyChatSettingResponse;
import com.example.forum.feature.chat.dto.chatResponseDto.ReadReceiptResponse;

import java.util.List;

public interface ChatParticipantService {
    List<MemberResponse> getMemberFromChat(Long chatId);
    List<MemberResponse> addNewMemberToChat(Long chatId, List<Long> memberIdList);
    MemberResponse changeMemberRole(Long chatId, Long memberId, ChangeRoleRequest request);
    ReadReceiptResponse readNewestChatMessage(Long chatId);
    MyChatSettingResponse updateChatSetting(Long chatId, UpdateChatSettingRequest request);
    void removeMemberFromChat(Long chatId, Long memberId);
    void leaveChat(Long chatId);
}
