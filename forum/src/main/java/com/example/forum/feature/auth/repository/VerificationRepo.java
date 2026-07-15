package com.example.forum.feature.auth.repository;

import com.example.forum.domain.UserVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationRepo extends JpaRepository<UserVerificationToken, Long> {
    Optional<UserVerificationToken> findByUser_Email(String email);

}
