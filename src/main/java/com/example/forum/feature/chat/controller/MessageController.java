package com.example.forum.feature.chat.controller;


import com.example.forum.common.dto.ApiResponse;
import com.example.forum.core.annotation.RateLimit;
import com.example.forum.feature.chat.service.MessageService;
import com.example.forum.feature.chat.dto.chatRequestDto.EditMessageRequest;
import com.example.forum.feature.chat.dto.chatRequestDto.MessageRequest;
import com.example.forum.feature.chat.dto.chatResponseDto.MessageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Message API")
@RestController
@RequestMapping("/forum/chats")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @RateLimit(capacity = 30, time = 1)
    @PostMapping("/{chatId}/messages")
    public ResponseEntity<MessageResponse> sendMessage(
            @PathVariable Long chatId,
            @Valid @RequestBody MessageRequest request) {

        MessageResponse response = messageService.sendMessage(chatId, request.getContent(), request.getReplyId());

        return ResponseEntity.ok(response);
    }

    @PostMapping( value = "/{chatId}/media-messages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MessageResponse>> sendMediaMessage(
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

    @RateLimit(capacity = 20, time = 1)
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

    @RateLimit(capacity = 20, time = 1)
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
