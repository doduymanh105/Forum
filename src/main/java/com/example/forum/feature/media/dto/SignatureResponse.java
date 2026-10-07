package com.example.forum.feature.media.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignatureResponse {
    
    private String signature;
    private Long timestamp;
    private String apiKey;
    private String cloudName;
}
