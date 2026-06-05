package com.example.forum.controller;

import com.example.forum.dto.request.chatRequestDto.CreateDirectChatRequest;
import com.example.forum.dto.request.chatRequestDto.CreateGroupChatRequest;
import com.example.forum.dto.request.chatRequestDto.UpdateChatRequest;
import com.example.forum.dto.response.chatResponseDto.ApiResponse;
import com.example.forum.dto.response.chatResponseDto.ChatMessageResponse;
import com.example.forum.dto.response.chatResponseDto.ChatResponse;
import com.example.forum.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping()
    public ResponseEntity<ApiResponse<?>> getChats(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false) String keyword
    ){
        return ResponseEntity.status(200)
                .body(ApiResponse.success(
                        "Get chat-list successfully!",
                        chatService.getChatLists(page, size, sortDir, sortBy, keyword)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getChatDetails(
            @PathVariable(value = "id") Long id
    ){
        return ResponseEntity.ok(ApiResponse.success(
                "Get chat setting details success",
                chatService.getChatDetails(id)
                )
        );
    }

    @PostMapping()
    public ResponseEntity<ApiResponse<?>> createDirectChat(
            @RequestBody CreateDirectChatRequest request
            ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Created chat successfully"
                , chatService.createChat(request.getId()))
        );
    }

    @PostMapping("/groupChats")
    public ResponseEntity<ApiResponse<ChatResponse>> createGroupChat(
            @RequestBody CreateGroupChatRequest request
    ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "GroupChat is created",
                        chatService.createGroupChat(request)
                )
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ChatResponse>> updateChatInfo(
            @PathVariable Long id,
            @RequestBody UpdateChatRequest request
            ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Chat updated",
                        chatService.updateChatInfo(request, id)
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteChat(
            @PathVariable Long id
    ){
        chatService.deleteChat( id);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Chat is deleted"
                )
        );
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> getMessages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword
    ){
        return ResponseEntity.ok(
                ApiResponse.success("Get messages successfully",
                        chatService.getChatMessage(id,page, size,keyword ))
        );
    }


}
