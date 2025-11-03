package com.ehb.connected.domain.impl.auth.models;

import com.ehb.connected.domain.impl.users.entities.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing a remember-me authentication token.
 * <p>
 * Security model:
 * - Tokens are hashed with BCrypt before storage (like passwords)
 * - Each user can have only one active token (old ones are deleted on login)
 * - Tokens expire after 30 days
 * - Tokens can be revoked early (logout, password change, etc.)
 */
@Entity
@Table(
    name = "remember_me_tokens",
    indexes = @Index(name = "idx_user_id", columnList = "user_id")
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RememberMeToken {

    /**
     * Composite primary key: user ID + token hash.
     * This allows efficient lookup by user and prevents duplicate tokens.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Hashed token value (BCrypt).
     * Never store tokens in plain text!
     */
    @Column(nullable = false, length = 60)
    private String tokenHash;

    /**
     * User who owns this token.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * When the token was created.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * When the token expires.
     * Stored in DB so we can revoke tokens before cookie expiry.
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Checks if this token has expired.
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
