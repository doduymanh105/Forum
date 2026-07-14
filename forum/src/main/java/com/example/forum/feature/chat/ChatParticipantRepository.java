package com.example.forum.feature.chat;


import com.example.forum.feature.chat.dto.chatResponseDto.ChatResponse;
import com.example.forum.feature.chat.dto.chatResponseDto.MemberResponse;
import com.example.forum.domain.Chat;
import com.example.forum.domain.ChatParticipant;
import com.example.forum.domain.ChatParticipantKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, ChatParticipantKey> {

//    com.example.forum.feature.chat.dto.chatResponseDto.ChatResponse
    @Query("""
            SELECT new com.example.forum.dto.response.chatResponseDto.ChatResponse (
                  c.id,
                  CASE
                    WHEN c.isGroup = true THEN c.chatName
                    ELSE (
                        SELECT u2.userName FROM ChatParticipant cp2 JOIN cp2.userEntity u2
                        WHERE cp2.chat.id = c.id and u2.userId <> :userId)
                  END,
                  CASE
                      WHEN c.isGroup = true THEN c.chatAvatarUrl
                      ELSE (
                            SELECT u2.avatarUrl FROM ChatParticipant cp2 JOIN cp2.userEntity u2
                            WHERE cp2.chat.id = c.id AND u2.userId <> :userId)
                  END,
                  c.isGroup,
                  c.createdAt,
                  cp.isPinned,
                  cp.isMuted,
                  cp.isArchived,
                  cp.lastReadMessageId,
                  c.lastMessageId,
                  c.lastSenderName,
                  c.lastMessageContent,
                  c.lastMessageType,
                  c.lastMessageAt,
                  cp.unreadCount
              )
              FROM ChatParticipant cp
              JOIN cp.chat c
              WHERE cp.userEntity.userId = :userId
              AND cp.isArchived = false
              AND (
                c.chatName LIKE CONCAT('%', :keyword, '%')
                OR
                EXISTS(
                    SELECT 1 FROM ChatParticipant cp2
                    JOIN cp2.userEntity u2
                    WHERE cp2.chat.id = c.id
                        AND u2.userId <> :userId
                        AND u2.userName LIKE CONCAT('%', :keyword, '%')
                )
              )
              ORDER BY cp.isPinned DESC, c.lastMessageAt DESC
            """)
    Page<ChatResponse> findAllByUserId(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
            SELECT cp
            FROM ChatParticipant cp
            JOIN FETCH cp.chat c
            WHERE cp.userEntity.userId = :userId
            AND c.id = :chatId
            """)
    Optional<ChatParticipant> findByUserIdAndChatId(
            @Param("userId") Long userId,
            @Param("chatId") Long chatId
    );

    @Query("""
        SELECT c
        FROM Chat c
        JOIN c.participantList p1
        JOIN c.participantList p2
        WHERE c.isGroup = false
        AND p1.userEntity.userId = :user1Id
        AND p2.userEntity.userId = :user2Id
    """)
    Optional<Chat> findPrivateChatBetween(
            @Param("user1Id") Long user1Id,
            @Param("user2Id") Long user2Id
    );

//    @Query("""
//            SELECT cp
//            FROM ChatParticipant cp
//            JOIN FETCH cp.user
//            WHERE cp.chat.id = :chatId
//            AND cp.user.id <> :userId
//            """)
//    Optional<ChatParticipant> findOtherParticipantByChatIAndUserId(
//            @Param("chatId") Long chatId,
//            @Param("userId") Long userId
//    );

    @Query("""
            SELECT cp
            FROM ChatParticipant cp
            JOIN FETCH cp.userEntity
            WHERE cp.chat.id = :chatId
            AND cp.userEntity.userId = :userId
            """)
    Optional<ChatParticipant> findMyParticipantByChatIAndUserId(
            @Param("chatId") Long chatId,
            @Param("userId") Long userId
    );

    @Query("""
            SELECT new com.example.forum.dto.response.chatResponseDto.MemberResponse(
                u.id,
                u.userName,
                u.avatarUrl,
                cp.role,
                cp.lastReadMessageId,
                cp.createdAt
            )
            FROM ChatParticipant cp
            JOIN cp.userEntity u
            WHERE cp.chat.id = :chatId
            """)
    List<MemberResponse> findAllMemberByChatId(
            @Param("chatId") Long chatId);

    @Query(
            """
               SELECT cp.userEntity.userId
               FROM ChatParticipant cp
               WHERE cp.chat.id = :chatId
               """
    )
    List<Long> findAllUserIdsByChatId(Long chatId);

    @Query(
            """
            SELECT COUNT(cp)
            FROM ChatParticipant cp
            WHERE cp.chat.id =: chatId
            AND cp.role = 'ADMIN'
            """
    )
    long countAdminByChatId(Long chatId);

    List<ChatParticipant> findAllByChatId(Long chatId);
}
