package com.example.forum.feature.vote;

import com.example.forum.common.dto.ApiResponse;
import com.example.forum.core.annotation.RateLimit;
import com.example.forum.domain.Enum.VoteType;
import com.example.forum.feature.vote.dto.PostVoteResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Post Vote API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/forum/posts")
public class VoteController {
    private final VoteService voteService;

    @RateLimit(capacity = 20, time = 1)
    @PostMapping("/{post_id}/vote")
    ResponseEntity<ApiResponse<PostVoteResponse>> vote (
            @PathVariable Long post_id,
            @RequestParam VoteType voteType
    ){
        return ResponseEntity.ok(ApiResponse.success(
                "Vote success",
                voteService.votePost(post_id, voteType)
        ));
    }

    @RateLimit(capacity = 100, time = 1)
    @GetMapping("/{post_id}/vote")
    ResponseEntity<ApiResponse<List<VoteProjection>>> getVoteByVoteType (
            @PathVariable Long post_id,
            @RequestParam VoteType voteType
            ) {

        return ResponseEntity.ok(ApiResponse.success(
                "list of vote by vote_type",
                voteService.findVoteOfPost(post_id, voteType)
        ));
    }
}
