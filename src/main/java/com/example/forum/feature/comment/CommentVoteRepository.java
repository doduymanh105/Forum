package com.example.forum.feature.comment;

import com.example.forum.domain.CommentVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentVoteRepository extends JpaRepository<CommentVote, Long> {

    Optional<CommentVote> findByCommentIdAndUserId(Long commentId, Long userId);

}
