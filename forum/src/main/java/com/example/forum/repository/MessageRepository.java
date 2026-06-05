package com.example.forum.repository;


import com.example.forum.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("""
        SELECT m FROM Message m
        WHERE m.chat.id = :chatId
        AND (:keyword IS NULL OR LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """)
    Page<Message> findByChatId(
            @Param("chatId") Long chatId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    Optional<Message> findTopByChatIdOrderBySendAtDesc(Long chatId);
}
