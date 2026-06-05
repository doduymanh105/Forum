package com.example.forum.dto.request.chatRequestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateChatRequest {
    @NotBlank(message = "Chat's name can not be blank!")
    @Size(max = 30, message = "Chat name can not exceed 30 characters")
    private String chatName;
}
