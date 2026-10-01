package com.example.forum.feature.vote;


import com.example.forum.core.config.RabbitMqConfig;
import com.example.forum.domain.Enum.EventType;
import com.example.forum.domain.Enum.VoteType;
import com.example.forum.domain.NotificationEvent;
import com.example.forum.domain.PostEntity;
import com.example.forum.domain.UserEntity;
import com.example.forum.domain.Vote;
import com.example.forum.feature.follow.dto.PendingNotification;
import com.example.forum.feature.notification.NotificationService;
import com.example.forum.feature.post.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoteBatchWorker {

    private final VoteRepository voteRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    private final RabbitTemplate rabbitTemplate;

    @Transactional
    @RabbitListener(
            queues = RabbitMqConfig.VOTE_QUEUE,
            containerFactory = "batchContainerFactory")
    public void processVoteBatch(List<VoteMessage> messages){
        log.info("[WORKER]: Start processing Vote batch", messages.size());

        if(messages.isEmpty()) return;

        Map<String, VoteMessage> distinctVotes = new HashMap<>();
        for(VoteMessage message : messages){
            String key = message.userId() + "-" + message.postId();
            distinctVotes.put(key, message);
        }

        List<Long> userIds = distinctVotes.values().stream().map(VoteMessage::userId).distinct().toList();
        List<Long> postIds = distinctVotes.values().stream().map(VoteMessage::postId).distinct().toList();

        List<Vote> existingVotes = voteRepository.findByUserEntityUserIdInAndPostEntityPostIdIn(userIds, postIds);
        
        Map<String, Vote> existingVoteMap = existingVotes.stream()
                .collect(Collectors.toMap(
                        v-> v.getUserEntity().getUserId() + "-" + v.getPostEntity().getPostId(),
                        v -> v
                ));
        
        Map<Long, Long> upvoteDeltas = new HashMap<>();
        Map<Long, Long> downvoteDeltas = new HashMap<>();
        List<Vote> voteToSave = new ArrayList<>();
        List<Vote> voteToDelete = new ArrayList<>();
        Map<Long, NotificationEvent> notificationEventMap = new HashMap<>();
        List<PendingNotification> pendingNotifications = new ArrayList<>();
        
        for(VoteMessage msg : distinctVotes.values()){
            Vote existing = existingVoteMap.get(msg.userId() + "-" + msg.postId());
            VoteType newVote = VoteType.valueOf(msg.voteType());
            Long pId = msg.postId();
            
            if(existing == null){
                if(newVote != VoteType.NONE){
                    voteToSave.add(
                            Vote.builder()
                                    .userEntity(UserEntity.builder().userId(msg.userId()).build())
                                    .postEntity(PostEntity.builder().postId(msg.postId()).build())
                                    .voteType(newVote)
                                    .build()
                    );
                    if(newVote == VoteType.UPVOTE) upvoteDeltas.merge(pId, 1L, Long::sum);
                    if(newVote == VoteType.DOWNVOTE) downvoteDeltas.merge(pId, 1L, Long::sum);
                }
            } else {
                VoteType oldVote = existing.getVoteType();
                if(newVote == VoteType.NONE || newVote == oldVote){
                    
                    voteToDelete.add(existing);
                    if(oldVote == VoteType.UPVOTE) upvoteDeltas.merge(pId, -1L, Long::sum);
                    if(oldVote == VoteType.DOWNVOTE) downvoteDeltas.merge(pId, -1L, Long::sum );
                } else {
                    existing.setVoteType(newVote);
                    voteToSave.add(existing);
                    
                    if(newVote ==VoteType.UPVOTE){
                        upvoteDeltas.merge(pId, 1L, Long::sum);
                        downvoteDeltas.merge(pId, -1L, Long::sum);
                    } else if (newVote == VoteType.DOWNVOTE) {
                        downvoteDeltas.merge(pId, -1L, Long::sum);
                        downvoteDeltas.merge(pId, 1L, Long::sum);
                    }
                }
                    
            }

            if(newVote != VoteType.NONE && (existing == null || existing.getVoteType() != newVote)) {

                UserEntity sender = UserEntity.builder().userId(msg.userId()).userName(msg.senderName()).build();
                UserEntity receiver = UserEntity.builder().userId(msg.postAuthorId()).build();

                NotificationEvent newNotificationEvent = notificationService.createEvent(
                        EventType.NEW_VOTE,
                        sender,
                        "You have " + newVote + " from " + sender.displayUsername(),
                        pId,
                        "POST");

                pendingNotifications.add(new PendingNotification(msg.postAuthorId(), newNotificationEvent));

            }

        }

        if(!voteToSave.isEmpty()) voteRepository.saveAll(voteToSave);
        if(!voteToDelete.isEmpty()) voteRepository.deleteAll(voteToDelete);


        Set<Long> allChangedPostIds = new HashSet<>();
        allChangedPostIds.addAll(upvoteDeltas.keySet());
        allChangedPostIds.addAll(downvoteDeltas.keySet());

        for(Long pId : allChangedPostIds){
            long upDelta = upvoteDeltas.getOrDefault(pId,0L);
            long downDelta = downvoteDeltas.getOrDefault(pId, 0L);
            if(upDelta != 0 || downDelta != 0){
                postRepository.updatePostScores(pId,upDelta,downDelta);
            }
        }
        for(PendingNotification pNoti : pendingNotifications){
            notificationService.notifySpecificUser(UserEntity.builder().userId(pNoti.targetUserId()).build(), pNoti.notificationEvent());
        }

        log.info("[WORKER] Completed Vote batch. Save: {}, Delete: {}, Post Updated: {}", voteToSave.size(), voteToDelete.size(), allChangedPostIds.size());

    }
}
