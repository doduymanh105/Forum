package com.example.forum.controller;


import com.example.forum.dto.request.chatRequestDto.EditMessageRequest;
import com.example.forum.dto.request.chatRequestDto.MessageRequest;
import com.example.forum.dto.response.chatResponseDto.ApiResponse;
import com.example.forum.dto.response.chatResponseDto.MessageResponse;
import com.example.forum.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/{chatId}/messages")
    public ResponseEntity<MessageResponse> sendMessage(
            @PathVariable Long chatId,
            @Valid @RequestBody MessageRequest request) {

        MessageResponse response = messageService.sendMessage(chatId, request.getContent(), request.getReplyId());

        return ResponseEntity.ok(response);
    }
    @PostMapping( value = "/{chatId}/media-messages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> sendMediaMessage(
            @PathVariable Long chatId,
            @RequestPart("file") MultipartFile file
    ){
        return ResponseEntity.ok(
                ApiResponse.success(

                        "Send media message success",
                        messageService.sendMediaMessage(chatId, file)
                )
        );
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
