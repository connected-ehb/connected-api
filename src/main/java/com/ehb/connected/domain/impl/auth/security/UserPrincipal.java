package com.ehb.connected.domain.impl.auth.security;

import com.ehb.connected.domain.impl.users.entities.Role;
import com.ehb.connected.domain.impl.users.entities.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * Lightweight principal stored in Redis session.
 * Contains only essential user data to minimize session size and avoid serialization issues.
 * The database is the source of truth - this is just a session cache of minimal user info.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long canvasUserId;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private boolean emailVerified;
    private boolean enabled;
    private AuthenticationType authenticationType;

    /**
     * Creates a UserPrincipal from a User entity.
     *
     * @param user The user entity
     * @param authenticationType The authentication type
     * @return A lightweight principal
     */
    public static UserPrincipal fromUser(User user, AuthenticationType authenticationType) {
        Objects.requireNonNull(user, "User cannot be null");
        Objects.requireNonNull(authenticationType, "AuthenticationType cannot be null");

        return new UserPrincipal(
                user.getId(),
                user.getCanvasUserId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.isEmailVerified(),
                user.isEnabled(),
                authenticationType
        );
    }

    /**
     * Gets the authorities/permissions for this user.
     * Delegates to the Role enum which defines permissions.
     */
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role != null ? role.getAuthorities() : Collections.emptySet();
    }

    /**
     * Gets the principal name used for Spring Security.
     * For OAuth2 users: Canvas user ID
     * For form login users: Email address
     */
    @JsonIgnore
    public String getName() {
        if (authenticationType == AuthenticationType.OAUTH2 && canvasUserId != null) {
            return String.valueOf(canvasUserId);
        }
        return email;
    }

    /**
     * Checks if this principal matches the current state of a User entity.
     * Used for detecting stale session data that needs refreshing.
     */
    public boolean matchesUser(User user) {
        if (user == null) {
            return false;
        }

        return Objects.equals(this.userId, user.getId())
                && Objects.equals(this.role, user.getRole())
                && this.emailVerified == user.isEmailVerified()
                && this.enabled == user.isEnabled();
    }

    /**
     * Checks if the user is fully enabled (account enabled AND email verified).
     */
    @JsonIgnore
    public boolean isFullyEnabled() {
        return enabled && emailVerified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(userId, that.userId) && Objects.equals(canvasUserId, that.canvasUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, canvasUserId);
    }
}
