package com.example.forum.feature.chat.controller;


import com.example.forum.feature.chat.service.ChatParticipantService;
import com.example.forum.feature.chat.dto.chatRequestDto.AddNewMemberRequest;
import com.example.forum.feature.chat.dto.chatRequestDto.ChangeRoleRequest;
import com.example.forum.feature.chat.dto.chatRequestDto.UpdateChatSettingRequest;
import com.example.forum.feature.chat.dto.chatResponseDto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatParticipantController {

    private final ChatParticipantService chatParticipantService;

    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<?>> getChatMembers(
            @PathVariable Long id
    ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get members successfully"
                , chatParticipantService.getMemberFromChat(id))
        );
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<ApiResponse<?>> addMemberToChat(
            @PathVariable Long id,
            @RequestBody AddNewMemberRequest request
            ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Members added successfully"
                        , chatParticipantService.addNewMemberToChat(id, request.getMemberIdList()))
        );
    }

    @PatchMapping("/{id}/members/{memId}")
    public ResponseEntity<ApiResponse<?>> changeMemberRole(
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
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<?>> readChatMessage(
            @PathVariable Long id
    ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Chat marked as read"
                        , chatParticipantService.readNewestChatMessage(id))
        );
    }

    @PatchMapping("/{id}/setting")
    public ResponseEntity<ApiResponse<?>> updateChatSetting(
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
