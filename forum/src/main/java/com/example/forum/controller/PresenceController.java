package com.example.forum.controller;

import com.example.forum.dto.response.ApiResponse;
import com.example.forum.service.OnlineOfflineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/forum/presence")
@RequiredArgsConstructor
public class PresenceController {

    private final OnlineOfflineService onlineOfflineService;

    @PostMapping("/check")
    public ResponseEntity<ApiResponse<?>> checkOnlineUsers(@RequestBody List<Long> userIds) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Online user",
                        onlineOfflineService.checkOnlineStatus(userIds)
                )
        );
    }

}
