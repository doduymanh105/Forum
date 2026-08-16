package com.example.forum.feature.notification;

import com.example.forum.common.dto.PagedResponse;
import com.example.forum.common.utils.SecurityUtils;
import com.example.forum.common.dto.ApiResponse;
import com.example.forum.feature.notification.dto.NotificationDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notification API")
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
    ResponseEntity<ApiResponse<PagedResponse<NotificationDto>>> getNotificationWithReadStatus(
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
    ResponseEntity<ApiResponse<Long>> getNumberOfNotifications(){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Number of unread notifications",
                        notificationService.countUnreadNotifications()
                )
        );
    }

    @PatchMapping("/me/notification/markAllRead")
    ResponseEntity<ApiResponse<?>> markAllRead(){
        notificationService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.success(
                "Marked All notification as read",
                null
        ));
    }
    @PatchMapping("/me/notification/{id}")
    ResponseEntity<ApiResponse<?>> markAsRead(
            @PathVariable Long id){
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Marked as read",
                null
        ));
    }

    @PatchMapping("/me/notification/{id}/archive")
    public ResponseEntity<ApiResponse<?>> archiveNotification(@PathVariable Long id) {
        notificationService.archiveNotification(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Notification archived"
        ));
    }

    @DeleteMapping("/me/notification/{id}")
    public ResponseEntity<ApiResponse<?>> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Notification deleted"
        ));
    }


}
