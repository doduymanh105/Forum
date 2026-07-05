package com.example.forum.service;



import com.example.forum.dto.request.chatRequestDto.ChangeRoleRequest;
import com.example.forum.dto.request.chatRequestDto.UpdateChatSettingRequest;
import com.example.forum.dto.response.chatResponseDto.MemberResponse;
import com.example.forum.dto.response.chatResponseDto.MyChatSettingResponse;
import com.example.forum.dto.response.chatResponseDto.ReadReceiptResponse;

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
