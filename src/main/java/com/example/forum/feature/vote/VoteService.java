package com.example.forum.feature.vote;

import com.example.forum.feature.vote.dto.PostVoteResponse;
import com.example.forum.domain.Enum.VoteType;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

public interface VoteService {
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    PostVoteResponse votePost(Long postId, VoteType voteType);

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    List<VoteProjection> findVoteOfPost(Long postId, VoteType voteType);
}
