package com.example.forum.feature.notification;

import com.example.forum.common.utils.SecurityUtils;
import com.example.forum.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/forum/user")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;

//    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public SseEmitter subscribe(){
//        Long currentUserId = securityUtils.getCurrentUserId();
//        return sseService.subscribe(currentUserId);
//    }




    @GetMapping("/me/notification")
    ResponseEntity<?> getNotificationWithReadStatus(
            @RequestParam(defaultValue = "0",required = false) int page,
            @RequestParam(defaultValue = "10",required = false) int size,
            @RequestParam(defaultValue = "", required = false) String keyword,
            @RequestParam(required = false) Boolean isRead
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "List of notification",
                        notificationService.getNotificationsWithReadStatus(page,size,keyword,isRead)
                )
        );
    }

    @GetMapping("/me/notification/count")
    ResponseEntity<?> getNumberOfNotifications(){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Number of unread notifications",
                        notificationService.countUnreadNotifications()
                )
        );
    }

    @PatchMapping("/me/notification/markAllRead")
    ResponseEntity<?> markAllRead(){
        notificationService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.success(
                "Marked All notification as read",
                null
        ));
    }
    @PatchMapping("/me/notification/{id}")
    ResponseEntity<?> markAsRead(
            @PathVariable Long id){
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Marked as read",
                null
        ));
    }

    @PatchMapping("/me/notification/{id}/archive")
    public ResponseEntity<ApiResponse<Void>> archiveNotification(@PathVariable Long id) {
        notificationService.archiveNotification(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Notification archived"
        ));
    }

    @DeleteMapping("/me/notification/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Notification deleted"
        ));
    }


}
