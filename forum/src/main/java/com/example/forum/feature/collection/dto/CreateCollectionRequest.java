package com.example.forum.feature.collection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCollectionRequest {

    @NotBlank
    @Size(min = 3, max = 20)
    private String name;

    private String thumbnailUrl;
}
