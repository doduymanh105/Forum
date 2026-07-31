package com.example.forum.feature.comment;

import com.example.forum.common.constant.MessageConstants;
import com.example.forum.common.utils.SecurityUtils;
import com.example.forum.core.exception.ResourceNotFoundException;
import com.example.forum.domain.CommentEntity;
import com.example.forum.domain.CommentVote;
import com.example.forum.domain.Enum.VoteType;
import com.example.forum.domain.UserEntity;
import com.example.forum.feature.comment.dto.CommentVoteResponse;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentVoteServiceImpl implements CommentVoteService{

    private final CommentVoteRepository commentVoteRepository;
    private final CommentRepository commentRepository;

    private final SecurityUtils securityUtils;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public CommentVoteResponse voteComment(Long commentId, VoteType requestVoteType) {

        UserEntity user = securityUtils.getCurrentUser();

        CommentEntity commentEntity = commentRepository.findById(commentId)
                .orElseThrow(()-> new ResourceNotFoundException(MessageConstants.COMMENT_NOT_FOUND));

        Optional<CommentVote> existingVoteOpt = commentVoteRepository.findByCommentIdAndUserId(commentId,user.getUserId());

        VoteType finalVote;
        if(existingVoteOpt.isPresent()){
            CommentVote existingVote = existingVoteOpt.get();
            if(existingVote.getVoteType() == requestVoteType){
                commentVoteRepository.delete(existingVote);
                finalVote = null;
            } else {
                existingVote.setVoteType(requestVoteType);
                commentVoteRepository.save(existingVote);
                finalVote = requestVoteType;
            }
        } else {
            CommentVote newVote = CommentVote.builder()
                    .commentId(commentId)
                    .userId(user.getUserId())
                    .voteType(requestVoteType)
                    .build();
            commentVoteRepository.save(newVote);
            finalVote = requestVoteType;
        }

        commentVoteRepository.flush();

        entityManager.refresh(commentEntity);

        return CommentVoteResponse.builder()
                .commentId(commentEntity.getCommentId())
                .upvotes(commentEntity.getUpvotes())
                .downvotes(commentEntity.getDownvotes())
                .score(commentEntity.getScore())
                .userVote(finalVote)
                .build();
    }
}
