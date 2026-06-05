package com.example.forum.dto.request.chatRequestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EditMessageRequest {

    @NotBlank
    private String content;
}
