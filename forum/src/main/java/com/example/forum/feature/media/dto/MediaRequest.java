package com.example.forum.feature.media.dto;

import com.example.forum.domain.Enum.MediaType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MediaRequest {
    @NotBlank(message = "Media URL cannot be blank")
    private String url;

    @NotBlank(message = "Media type cannot be blank")
    private MediaType type;

    private Long size;
}
