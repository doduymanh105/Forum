package com.example.forum.feature.post.dto;

public record ToxicityCheckResult(
        boolean isViolating,
        String reason
) {}
