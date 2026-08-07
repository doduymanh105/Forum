package com.example.forum.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
//@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ChatParticipantKey implements Serializable {
    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "user_id")
    private Long userId;

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatParticipantKey that = (ChatParticipantKey) o;
        return Objects.equals(chatId, that.chatId)  && Objects.equals(userId, that.userId);
    }
    @Override
    public int hashCode() {
        return Objects.hash(chatId, userId);
    }
}

