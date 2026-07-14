package com.example.forum.feature.chat;

import com.example.forum.domain.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    Optional<Chat> findByIdAndIsDeletedFalse(Long chatId);
}
