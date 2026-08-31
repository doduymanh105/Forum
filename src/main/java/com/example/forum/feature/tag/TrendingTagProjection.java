package com.example.forum.feature.tag;

public interface TrendingTagProjection {
    Long getTagId();
    String getTagName();
    Long getTotalScore();
    Long getTotalPost();
}
