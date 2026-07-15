package com.example.forum.feature.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestWsController {

    private final SimpMessagingTemplate template;

    @GetMapping("/api/test-ws")
    public String testWs(){
        template.convertAndSend("/topic/test", "Chào 500 anh em! Tin nhắn này được bắn từ REST API lúc " + System.currentTimeMillis());
        return "Đã ra lệnh cho WebSocket bắn tin!";
    }
}
