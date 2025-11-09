package com.ehb.connected.domain.impl.auth.repositories;

import com.ehb.connected.domain.impl.auth.models.RememberMeToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for managing remember-me authentication tokens.
 * <p>
 * Note: Tokens are hashed before storage, so we look up by user ID
 * and then compare hashes using BCrypt.
 */
public interface RememberMeTokenRepository extends JpaRepository<RememberMeToken, Long> {

    /**
     * Find all tokens for a specific user.
     * Used to validate remember-me tokens (we check hash with BCrypt).
     */
    List<RememberMeToken> findByUserId(Long userId);

    /**
     * Delete all remember-me tokens for a user.
     * Called on logout or password change.
     */
    void deleteByUserId(Long userId);
}
