package com.example.forum.controller;


import com.example.forum.dto.request.chatRequestDto.EditMessageRequest;
import com.example.forum.dto.request.chatRequestDto.MessageRequest;
import com.example.forum.dto.response.chatResponseDto.ApiResponse;
import com.example.forum.dto.response.chatResponseDto.MessageResponse;
import com.example.forum.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/{chatId}/messages")
    public ResponseEntity<MessageResponse> sendMessage(
            @PathVariable Long chatId,
            @Valid @RequestBody MessageRequest request) {

        MessageResponse response = messageService.sendMessage(chatId, request.getContent());

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/messages/{id}")
    public ResponseEntity<ApiResponse<MessageResponse>> editMessage(
            @RequestBody EditMessageRequest request,
            @PathVariable Long id
            ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Message is updated"
                , messageService.editMessage(request, id))
        );
    }
    @DeleteMapping("/messages/{id}")
    public ResponseEntity<ApiResponse<MessageResponse>> deleteMessage(
            @RequestBody EditMessageRequest request,
            @PathVariable Long id
    ){
        messageService.deleteMessage(id);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Message is deleted"
                )
        );
    }
}
