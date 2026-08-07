package com.example.forum.feature.collection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectionContentResponse {
    private int totalElement;
    private List<PostPreviewDto> postPreviewDtoList;

}
