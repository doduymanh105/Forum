package com.example.forum.feature.chat.controller;


import com.example.forum.common.dto.ApiResponse;
import com.example.forum.core.annotation.RateLimit;
import com.example.forum.feature.chat.dto.chatResponseDto.MemberResponse;
import com.example.forum.feature.chat.dto.chatResponseDto.MyChatSettingResponse;
import com.example.forum.feature.chat.dto.chatResponseDto.ReadReceiptResponse;
import com.example.forum.feature.chat.service.ChatParticipantService;
import com.example.forum.feature.chat.dto.chatRequestDto.AddNewMemberRequest;
import com.example.forum.feature.chat.dto.chatRequestDto.ChangeRoleRequest;
import com.example.forum.feature.chat.dto.chatRequestDto.UpdateChatSettingRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Chat Participant API")
@RestController
@RequestMapping("/forum/chats")
@RequiredArgsConstructor
public class ChatParticipantController {

    private final ChatParticipantService chatParticipantService;

    @RateLimit(capacity = 100, time = 1)
    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> getChatMembers(
            @PathVariable Long id
    ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get members successfully"
                , chatParticipantService.getMemberFromChat(id))
        );
    }

    @RateLimit(capacity = 20, time = 1)
    @PostMapping("/{id}/members")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> addMemberToChat(
            @PathVariable Long id,
            @RequestBody AddNewMemberRequest request
            ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Members added successfully"
                        , chatParticipantService.addNewMemberToChat(id, request.getMemberIdList()))
        );
    }

    @RateLimit(capacity = 20, time = 1)
    @PatchMapping("/{id}/members/{memId}")
    public ResponseEntity<ApiResponse<MemberResponse>> changeMemberRole(
            @PathVariable Long id,
            @PathVariable Long memId,
            @RequestBody ChangeRoleRequest request
            ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Member role updated"
                        , chatParticipantService.changeMemberRole(id,memId,request ))
        );
    }
    @RateLimit(capacity = 20, time = 1)
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<ReadReceiptResponse>> readChatMessage(
            @PathVariable Long id
    ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Chat marked as read"
                        , chatParticipantService.readNewestChatMessage(id))
        );
    }

    @RateLimit(capacity = 20, time = 1)
    @PatchMapping("/{id}/setting")
    public ResponseEntity<ApiResponse<MyChatSettingResponse>> updateChatSetting(
            @PathVariable Long id,
            @RequestBody UpdateChatSettingRequest request
    ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Personal chat's settings updated",
                        chatParticipantService.updateChatSetting(id, request)
                )
        );
    }

    @RateLimit(capacity = 20, time = 1)
    @DeleteMapping("/{id}/members/{memId}")
    public ResponseEntity<ApiResponse<?>> removeMemberFromGroup(
            @PathVariable Long id,
            @PathVariable Long memId
    ){
        chatParticipantService.removeMemberFromChat(id,memId );
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                ApiResponse.success(
                        "Member has been kicked from group"
                )
        );
    }

    @RateLimit(capacity = 20, time = 1)
    @DeleteMapping("/{id}/members/me")
    public ResponseEntity<ApiResponse<?>> leaveChat(
            @PathVariable Long id
    ){
        chatParticipantService.leaveChat(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                ApiResponse.success(
                        "You have left the chat"
                )
        );
    }


}
