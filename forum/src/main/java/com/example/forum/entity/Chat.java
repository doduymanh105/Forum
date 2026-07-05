package com.example.forum.entity;


import com.example.forum.entity.Enum.MessageType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Entity
@Table(name = "chats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_name", length = 100)
    private String chatName;

    @Column(name = "is_group")
    private boolean isGroup = false;

    @Column(name = "last_message_id")
    private Long lastMessageId;

    @Column(name = "chat_avatar_url")
    private String chatAvatarUrl;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "last_sender_name")
    private String lastSenderName;

    @Column(name = "last_message_content", length = 2000) // Lưu preview
    private String lastMessageContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_message_type")
    private MessageType lastMessageType;

    @Column(name = "is_deleted")
    private boolean isDeleted=false;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "chat", fetch = FetchType.LAZY)
    @JsonIgnoreProperties("chat")
    private List<ChatParticipant> participantList;
}
