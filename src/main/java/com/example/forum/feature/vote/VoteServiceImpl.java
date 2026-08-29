package com.example.forum.feature.vote;

import com.example.forum.core.exception.AppException;
import com.example.forum.core.exception.ErrorCode;
import com.example.forum.feature.vote.dto.PostVoteResponse;
import com.example.forum.domain.Enum.EventType;
import com.example.forum.domain.NotificationEvent;
import com.example.forum.domain.PostEntity;
import com.example.forum.domain.UserEntity;
import com.example.forum.domain.Vote;
import com.example.forum.domain.Enum.VoteType;
import com.example.forum.feature.post.PostRepository;
import com.example.forum.common.utils.SecurityUtils;
import com.example.forum.feature.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class VoteServiceImpl implements VoteService {

    private final PostRepository postRepository;
    private final SecurityUtils securityService;
    private final VoteRepository voteRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public PostVoteResponse votePost(Long postId, VoteType newVote) {

        PostEntity post = postRepository.findByPostId(postId)
                .orElseThrow(()-> new AppException(ErrorCode.POST_NOT_FOUND));

        UserEntity currentUser = securityService.getCurrentUser();
        Long currentUserId= currentUser.getUserId();

        var existingVote = voteRepository.findByUserEntityUserIdAndPostEntityPostId(currentUserId, postId);

        VoteType finalVote;

        if(existingVote.isEmpty()) {
            if (newVote == VoteType.NONE) {
                return new PostVoteResponse(post.getPostId(), post.getUpvotes(), post.getDownvotes(), post.getUpvotes() - post.getDownvotes(), VoteType.NONE);
            }
            finalVote = createVote(post, currentUser, newVote);
        } else {
            Vote vote = existingVote.get();
            if (newVote == VoteType.NONE || vote.getVoteType() == newVote) {
                finalVote = cancelVote(post, vote, newVote);
            } else {
                finalVote = changeVote(post, vote, newVote);
            }
        }

        postRepository.save(post);

        NotificationEvent newNotificationEvent = notificationService.createEvent(
                EventType.NEW_VOTE,
                currentUser,
                "You have " + finalVote + " from " + currentUser.displayUsername(),
                post.getPostId(),
                "POST");

        notificationService.notifySpecificUser(post.getCreator(), newNotificationEvent);

        return new PostVoteResponse(
                post.getPostId(),
                post.getUpvotes(),
                post.getDownvotes(),
                post.getUpvotes() - post.getDownvotes(),
                finalVote
        );
    }

    @Override
    public List<VoteProjection> findVoteOfPost(Long postId, VoteType voteType) {
        PostEntity post = postRepository.findByPostId(postId)
                .orElseThrow(()-> new AppException(ErrorCode.POST_NOT_FOUND));
        return voteRepository.findVotesOfPost(postId, voteType.name());
    }

    private VoteType createVote (PostEntity post, UserEntity user, VoteType newVote) {
        Vote vote = Vote.builder()
                .postEntity(post)
                .voteType(newVote)
                .userEntity(user)
                .build();

        if (newVote == VoteType.UPVOTE) post.setUpvotes(post.getUpvotes() + 1);
        else if (newVote == VoteType.DOWNVOTE) post.setDownvotes(post.getDownvotes() + 1);

        voteRepository.save(vote);
        return newVote;
    }

    private VoteType cancelVote(PostEntity post, Vote existingVote, VoteType currentVote) {
        if (currentVote == VoteType.UPVOTE) post.setUpvotes(Math.max(0, post.getUpvotes() - 1));
        else if (currentVote == VoteType.DOWNVOTE) post.setDownvotes(Math.max(0, post.getDownvotes() - 1));
        voteRepository.delete(existingVote);
        return VoteType.NONE;
    }

    private VoteType changeVote (PostEntity post, Vote existingVote, VoteType newVote) {
        if (newVote == VoteType.UPVOTE) {
            post.setDownvotes(Math.max(0, post.getDownvotes() - 1));
            post.setUpvotes(post.getUpvotes() + 1);
        } else if (newVote == VoteType.DOWNVOTE) {
            post.setUpvotes(Math.max(0, post.getUpvotes() - 1));
            post.setDownvotes(post.getDownvotes() + 1);
        }

        existingVote.setVoteType(newVote);
        voteRepository.save(existingVote);
        return newVote;
    }
}
