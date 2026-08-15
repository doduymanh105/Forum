package com.example.forum.feature.chat.controller;

import com.example.forum.common.dto.ApiResponse;
import com.example.forum.feature.chat.service.OnlineOfflineService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "Presence API")
@RestController
@RequestMapping("/forum/presence")
@RequiredArgsConstructor
public class PresenceController {

    private final OnlineOfflineService onlineOfflineService;

    @PostMapping("/check")
    public ResponseEntity<ApiResponse<Map<Long, Boolean>>> checkOnlineUsers(@RequestBody List<Long> userIds) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Online user",
                        onlineOfflineService.checkOnlineStatus(userIds)
                )
        );
    }

}
