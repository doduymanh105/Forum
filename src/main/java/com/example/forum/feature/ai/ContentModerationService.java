package com.example.forum.feature.ai;

import com.example.forum.core.exception.AppException;
import com.example.forum.core.exception.ErrorCode;
import com.example.forum.feature.post.dto.ToxicityCheckResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentModerationService {

    private final GenerativeAiService aiService;

    public void validateContentStrictly(String title, String content) {
        ToxicityCheckResult result = aiService.checkContentPolicy(title, content);

        if (result.isViolating()) {
            String detailedMessage = ErrorCode.POST_CONTENT_TOXIC.getMessage() + ": " + result.reason();
            log.error(detailedMessage);
            throw new AppException(ErrorCode.POST_CONTENT_TOXIC, detailedMessage);
        }
    }

}
