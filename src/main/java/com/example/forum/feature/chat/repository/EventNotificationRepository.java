package com.example.forum.feature.chat.repository;

import com.example.forum.domain.NotificationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventNotificationRepository extends JpaRepository<NotificationEvent, Long> {
}
