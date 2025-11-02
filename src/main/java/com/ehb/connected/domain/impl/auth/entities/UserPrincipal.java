package com.ehb.connected.domain.impl.auth.entities;

import com.ehb.connected.domain.impl.users.entities.Role;
import com.ehb.connected.domain.impl.users.entities.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.Collection;
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
    private static final long serialVersionUID = 1L;

    /**
     * User's database ID (primary key).
     */
    private Long userId;

    /**
     * Canvas user ID (nullable - only for OAuth2 users).
     */
    private Long canvasUserId;

    /**
     * User's email address.
     */
    private String email;

    /**
     * User's first name.
     */
    private String firstName;

    /**
     * User's last name.
     */
    private String lastName;

    /**
     * User's role (STUDENT, TEACHER, RESEARCHER, ADMIN).
     */
    private Role role;

    /**
     * Whether the user's email has been verified.
     */
    private boolean emailVerified;

    /**
     * Whether the user's account is enabled.
     */
    private boolean enabled;

    /**
     * Type of authentication used (OAUTH2 or FORM).
     */
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
     * Note: This delegates to the Role enum which defines permissions.
     *
     * @return Collection of granted authorities
     */
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) {
            return java.util.Collections.emptySet();
        }
        return role.getAuthorities();
    }

    /**
     * Gets the principal name used for Spring Security.
     * For OAuth2 users: Canvas user ID
     * For form login users: Email address
     *
     * @return The principal name
     */
    @JsonIgnore
    public String getName() {
        if (authenticationType == AuthenticationType.OAUTH2 && canvasUserId != null) {
            return String.valueOf(canvasUserId);
        }
        return email;
    }

    /**
     * Checks if this principal represents the same user as another User entity.
     * Useful for detecting stale session data.
     *
     * @param user The user to compare
     * @return true if the essential data matches
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
     * This mirrors the logic in User.isEnabled().
     *
     * @return true if account is enabled and email is verified
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
