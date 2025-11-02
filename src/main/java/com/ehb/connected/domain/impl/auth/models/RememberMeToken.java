package com.ehb.connected.domain.impl.auth.models;

import com.ehb.connected.domain.impl.users.entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing a remember-me authentication token.
 * Tokens are stored hashed for security and expire after 30 days.
 */
@Entity
@Table(name = "remember_me_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RememberMeToken {

    @Id
    @Column(length = 256, nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Checks if this token has expired.
     * @return true if current time is after expiration time
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
