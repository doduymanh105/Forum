package com.example.forum.feature.ai;

import com.example.forum.core.config.RabbitMqConfig;
import com.example.forum.core.exception.AppException;
import com.example.forum.core.exception.ErrorCode;
import com.example.forum.domain.Enum.EventType;
import com.example.forum.domain.Enum.PostStatus;
import com.example.forum.domain.NotificationEvent;
import com.example.forum.domain.PostEntity;
import com.example.forum.feature.notification.NotificationService;
import com.example.forum.feature.post.PostRepository;
import com.example.forum.feature.post.dto.PostModerationMessage;
import com.example.forum.feature.post.dto.ToxicityCheckResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentModerationService {

    private final PostRepository postRepository;
    private final GenerativeAiService aiService;
    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMqConfig.POST_UPLOAD_QUEUE)
    @Transactional
    public void validateContentStrictly(PostModerationMessage msg) {
        log.info("[WORKER] Content moderation worker STARTED...");
        PostEntity post = postRepository.findById(msg.postId())
                .orElseThrow(()-> new AppException(ErrorCode.POST_NOT_FOUND));

        ToxicityCheckResult result = aiService.checkContentPolicy(post.getPostTitle(), post.getPostContent());

        if (result.isViolating()) {
            String detailedMessage = ErrorCode.POST_CONTENT_TOXIC.getMessage() + ": " + result.reason();
            post.setStatus(PostStatus.REJECTED);

            NotificationEvent rejectedNotificationEvent = notificationService.createEvent(
                    EventType.POST_REJECTED,
                    post.getCreator(),
                    detailedMessage,
                    post.getPostId(),
                    "POST");

            notificationService.notifySpecificUser(post.getCreator(), rejectedNotificationEvent);
            log.error(detailedMessage);
            log.warn("[AI MODERATION] Post {} rejected: {}", msg.postId(), result.reason());
            return;
        }

        post.setStatus(PostStatus.PUBLISHED);
        NotificationEvent newNotificationEvent = notificationService.createEvent(
                EventType.POST_PUBLISHED,
                post.getCreator(),
                "Post published",
                post.getPostId(),
                "POST");

        notificationService.notifySpecificUser(post.getCreator(), newNotificationEvent );
        log.info("[WORKER] Content moderation worker FINISHED...");
    }

}
