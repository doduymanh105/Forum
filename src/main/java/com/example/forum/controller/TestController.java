package com.example.forum.controller;


import com.example.forum.core.config.RabbitMqConfig;
import com.example.forum.feature.vote.VoteMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/forum/home")
public class TestController {
    @GetMapping("/secured")
    public String secured(){
        return "welcome after logging in";
    }

    @GetMapping("/debug-auth")
    public void debugAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Principal type: " + auth.getPrincipal().getClass().getName());
        System.out.println("Principal value: " + auth.getPrincipal());
        System.out.println("Authorities: " + auth.getAuthorities());
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/test/pump-votes")
    public String testVoteBatching() {
        Long targetPostId = 8L; // ID của một bài viết có thật trong DB của cậu
        Long authorId = 2L;     // ID của tác giả bài viết đó

        // Bơm 55 lượt UPVOTE từ 55 user khác nhau
        for (long i = 1; i <= 55; i++) {
            VoteMessage upvoteMsg = new VoteMessage(
                    i, // Fake User ID (từ 1 đến 55)
                    targetPostId,
                    authorId,
                    "FakeUser" + i,
                    "UPVOTE"
            );
            rabbitTemplate.convertAndSend(RabbitMqConfig.VOTE_QUEUE, upvoteMsg);
        }

        // Bơm thêm 15 lượt DOWNVOTE từ 15 user khác
        for (long i = 56; i <= 70; i++) {
            VoteMessage downvoteMsg = new VoteMessage(
                    i,
                    targetPostId,
                    authorId,
                    "FakeUser" + i,
                    "DOWNVOTE"
            );
            rabbitTemplate.convertAndSend(RabbitMqConfig.VOTE_QUEUE, downvoteMsg);
        }

        return "Đã bơm thành công 70 tin nhắn Vote vào Queue!";
    }

}
