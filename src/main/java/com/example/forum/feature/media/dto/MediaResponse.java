package com.example.forum.feature.media.dto;

import com.example.forum.domain.Enum.MediaType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MediaResponse {
    private Long id;
    private String url;
    private MediaType type;
    private Long size;
}
