package com.example.forum.entity;

import com.example.forum.entity.Enum.ChatRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatParticipant {

    @EmbeddedId
    private ChatParticipantKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("chatId")
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private ChatRole role = ChatRole.MEMBER;

    @Column(name = "last_read_message_id")
    private Long lastReadMessageId;

    @Column(name = "unread_count")
    private int unreadCount = 0;

    private boolean isMuted = false;
    private boolean isPinned = false;
    private boolean isArchived = false;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Column(name = "added_by")
    private Long addedBy;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @CreationTimestamp
    private LocalDateTime createdAt;
}