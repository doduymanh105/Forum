package com.example.forum.feature.statistics.dto;

import com.example.forum.domain.Tag;
import com.example.forum.feature.collection.dto.PostPreviewDto;
import com.example.forum.feature.post.dto.PostResponseDto;
import com.example.forum.feature.tag.dto.TrendingTagDto;
import com.example.forum.feature.user.dto.UserSummaryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderBoardResponse {

    private List<TrendingTagDto> topTags;

    private List<TopUserDto> topActiveUsers;

    private List<PostPreviewDto> topPosts;

}
