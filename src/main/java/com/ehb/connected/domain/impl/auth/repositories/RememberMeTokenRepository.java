package com.ehb.connected.domain.impl.auth.repositories;

import com.ehb.connected.domain.impl.auth.models.RememberMeToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for managing remember-me authentication tokens.
 */
public interface RememberMeTokenRepository extends JpaRepository<RememberMeToken, String> {
    Optional<RememberMeToken> findByToken(String token);
    void deleteByUserId(Long userId);
}
