package com.example.forum.feature.user;

import com.example.forum.domain.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

}
