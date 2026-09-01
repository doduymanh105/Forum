package com.example.forum.feature.user;

import com.example.forum.domain.UserEntity;
import com.example.forum.feature.statistics.TopUserProjection;
import com.example.forum.feature.statistics.dto.TopUserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<UserEntity,Long> {
    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByProviderAndProviderId(String provider, String providerId);

    Optional<UserEntity> findById(Long id);

    Page<UserEntity> findByIsDeletedFalseAndUserNameContainingIgnoreCase(String keyword, Pageable pageable);

    @Query("""
    SELECT u
    FROM UserEntity u
    WHERE u.isDeleted = false
      AND (
            LOWER(u.userName) LIKE LOWER(CONCAT('%', :keyword, '%'))
         OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
      )
""")
    Page<UserEntity> searchByKeyword(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    boolean existsByUserName(String userName);

    Page<UserEntity> findAllByIsDeletedFalse(Pageable pageable);

    Long countByCreatedAtAfter(LocalDateTime now);


    Long countByIsDeletedFalse();

    Long countByIsDeletedFalseAndIsTwoFactorEnabledTrue();


    @Query(value = """
            SELECT TO_CHAR(created_at, 'YYYY-MM-DD') as dateStr,
            COUNT(*) as totalCount
            FROM users
            WHERE created_at >= :startDate
            AND created_at <= :endDate
            AND is_deleted = false
            GROUP BY TO_CHAR(created_at, 'YYYY-MM-DD')
            ORDER BY dateStr ASC
            """, nativeQuery = true)
    List<Object[]> countUsersGroupedByDate(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query(value = """
            SELECT
            u.user_id as userId,
            u.user_name as userName,
            u.email as email,
            u.avatar_url as avatarUrl,
            COUNT(p.post_id) as totalPosts
            FROM post_entity p
            JOIN users u
            ON p.creator_id = u.user_id
            WHERE p.created_at <= :endDate
            AND p.created_At >= :startDate
            GROUP BY u.user_id, u.user_name, u.email, u.avatar_url
            ORDER BY totalPosts DESC
            LIMIT 5
            """, nativeQuery = true)
    List<TopUserProjection> getTopUsers(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
