package com.ehb.connected.domain.impl.users.Factories;

import com.ehb.connected.domain.impl.canvas.entities.CanvasAttributes;
import com.ehb.connected.domain.impl.users.entities.Role;
import com.ehb.connected.domain.impl.users.entities.User;
import org.springframework.stereotype.Component;

/**
 * Factory for creating new User entities with proper defaults.
 */
@Component
public class UserFactory {

    /**
     * Creates a new user from Canvas OAuth2 attributes.
     * Email verification required (Canvas doesn't provide email).
     * Role must be set separately based on email domain.
     *
     * @param attrs Canvas user attributes
     * @return A new User entity for Canvas OAuth2 users
     */
    public User newCanvasUser(CanvasAttributes attrs) {
        User user = new User();
        user.setCanvasUserId(attrs.getId());
        user.setFirstName(attrs.getFirstName());
        user.setLastName(attrs.getLastName());
        user.setProfileImageUrl(attrs.getProfileImageUrl());

        // Canvas doesn't provide email - must be collected and verified
        user.setEmail(null);
        user.setRole(null);  // Set based on email domain after verification
        user.setEmailVerified(false);

        // Account enabled by default
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);

        return user;
    }

    /**
     * Creates a new user for form-based registration (researchers).
     * Email verification not required for now.
     *
     * @param email User's email address
     * @param firstName User's first name
     * @param lastName User's last name
     * @param encodedPassword BCrypt-encoded password
     * @return A new User entity for form-based login
     */
    public User newFormUser(String email, String firstName, String lastName, String encodedPassword) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(encodedPassword);

        // Form users are researchers (no Canvas account)
        user.setRole(Role.RESEARCHER);

        // Researchers don't need email verification (for now)
        user.setEmailVerified(true);

        // Account enabled by default
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);

        // No Canvas ID for form users
        user.setCanvasUserId(null);

        return user;
    }
}

