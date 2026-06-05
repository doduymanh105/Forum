package com.example.forum.entity;

import com.example.forum.entity.Enum.MessageType;
import jakarta.persistence.*;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages", indexes = {
        @Index(name ="idx_chat_send_at", columnList = "chat_id, send_at")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity sender;

    @Column(columnDefinition = "TEXT") // Postgres dùng TEXT, MySQL dùng LONGTEXT
    private String content;

    @Enumerated(EnumType.STRING)
    private MessageType type = MessageType.TEXT;

    @Column(name = "reply_to_message_id")
    private Long replyToMessageId;

    private boolean isDeleted = false;
    private boolean isEdited = false;

    @Column(name = "send_at")
    @CreationTimestamp
    private LocalDateTime sendAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

