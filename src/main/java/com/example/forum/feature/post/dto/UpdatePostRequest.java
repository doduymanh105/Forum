package com.example.forum.feature.post.dto;

import com.example.forum.feature.media.dto.MediaRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePostRequest {
    private String title;
    private String content;
    private Set<Long> tagSet;
    private List<MediaRequest> mediaRequestList;
}
