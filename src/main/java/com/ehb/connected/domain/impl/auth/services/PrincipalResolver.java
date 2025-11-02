package com.ehb.connected.domain.impl.auth.services;

import com.ehb.connected.domain.impl.auth.entities.CustomOAuth2User;
import com.ehb.connected.domain.impl.auth.entities.UserPrincipal;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.repositories.UserRepository;
import com.ehb.connected.exceptions.BaseRuntimeException;
import com.ehb.connected.exceptions.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.security.Principal;

/**
 * Service for resolving and extracting User entities from various Principal types.
 * Centralizes all principal extraction logic to avoid code duplication across services.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PrincipalResolver {

    private final UserRepository userRepository;

    /**
     * Extracts the UserPrincipal from the current security context.
     *
     * @return The lightweight UserPrincipal
     * @throws BaseRuntimeException if not authenticated or principal type is unexpected
     */
    public UserPrincipal getUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BaseRuntimeException("No authenticated user found", HttpStatus.UNAUTHORIZED);
        }
        return extractUserPrincipal(authentication);
    }

    /**
     * Extracts the UserPrincipal from an Authentication object.
     *
     * @param authentication The authentication object
     * @return The lightweight UserPrincipal
     * @throws BaseRuntimeException if principal type is unexpected
     */
    public UserPrincipal extractUserPrincipal(Authentication authentication) {
        if (authentication == null) {
            throw new BaseRuntimeException("Authentication is null", HttpStatus.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();

        // OAuth2 authentication with CustomOAuth2User
        if (principal instanceof CustomOAuth2User customOAuth2User) {
            return customOAuth2User.getUserPrincipal();
        }

        // Form-based authentication with UserDetails (User entity)
        if (principal instanceof UserDetails userDetails) {
            // User entity implements UserDetails
            if (userDetails instanceof User user) {
                return UserPrincipal.fromUser(user, com.ehb.connected.domain.impl.auth.entities.AuthenticationType.FORM);
            }
            // Fallback: load by username (email)
            return getUserPrincipalByEmail(userDetails.getUsername());
        }

        // Unexpected principal type
        throw new BaseRuntimeException(
                "Unexpected principal type: " + principal.getClass().getName(),
                HttpStatus.UNAUTHORIZED
        );
    }

    /**
     * Extracts the UserPrincipal from a Principal object.
     * Handles both OAuth2 and form-based authentication.
     *
     * @param principal The principal (from controller method parameter)
     * @return The lightweight UserPrincipal
     * @throws BaseRuntimeException if principal type is unexpected
     */
    public UserPrincipal extractUserPrincipal(Principal principal) {
        if (principal instanceof Authentication authentication) {
            return extractUserPrincipal(authentication);
        }

        // Fallback: treat as email-based principal
        return getUserPrincipalByEmail(principal.getName());
    }

    /**
     * Extracts the full User entity from the current security context.
     * Always loads fresh data from the database.
     *
     * @return The User entity
     * @throws EntityNotFoundException if user not found in database
     */
    public User getUser() {
        UserPrincipal userPrincipal = getUserPrincipal();
        return loadUserFromDatabase(userPrincipal);
    }

    /**
     * Extracts the full User entity from an Authentication object.
     * Always loads fresh data from the database.
     *
     * @param authentication The authentication object
     * @return The User entity
     * @throws EntityNotFoundException if user not found in database
     */
    public User getUser(Authentication authentication) {
        UserPrincipal userPrincipal = extractUserPrincipal(authentication);
        return loadUserFromDatabase(userPrincipal);
    }

    /**
     * Extracts the full User entity from a Principal object.
     * Always loads fresh data from the database.
     *
     * @param principal The principal
     * @return The User entity
     * @throws EntityNotFoundException if user not found in database
     */
    public User getUser(Principal principal) {
        UserPrincipal userPrincipal = extractUserPrincipal(principal);
        return loadUserFromDatabase(userPrincipal);
    }

    /**
     * Loads a User entity from the database using the UserPrincipal.
     *
     * @param userPrincipal The lightweight principal
     * @return The full User entity from database
     * @throws EntityNotFoundException if user not found
     */
    public User loadUserFromDatabase(UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            throw new BaseRuntimeException("UserPrincipal is null", HttpStatus.UNAUTHORIZED);
        }

        // Prefer lookup by database ID for efficiency
        if (userPrincipal.getUserId() != null) {
            return userRepository.findById(userPrincipal.getUserId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userPrincipal.getUserId()));
        }

        // Fallback: lookup by Canvas ID (for OAuth2 users)
        if (userPrincipal.getCanvasUserId() != null) {
            return userRepository.findByCanvasUserId(userPrincipal.getCanvasUserId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with Canvas ID: " + userPrincipal.getCanvasUserId()));
        }

        // Fallback: lookup by email
        if (userPrincipal.getEmail() != null) {
            return userRepository.findByEmail(userPrincipal.getEmail())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + userPrincipal.getEmail()));
        }

        throw new BaseRuntimeException("UserPrincipal has no identifiable fields", HttpStatus.UNAUTHORIZED);
    }

    /**
     * Creates a UserPrincipal by looking up a user by email.
     *
     * @param email The user's email
     * @return The UserPrincipal
     * @throws EntityNotFoundException if user not found
     */
    private UserPrincipal getUserPrincipalByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
        return UserPrincipal.fromUser(user, com.ehb.connected.domain.impl.auth.entities.AuthenticationType.FORM);
    }

    /**
     * Checks if the session principal is stale compared to the database.
     *
     * @param userPrincipal The principal from session
     * @return true if the session needs to be refreshed
     */
    public boolean isSessionStale(UserPrincipal userPrincipal) {
        User dbUser = loadUserFromDatabase(userPrincipal);
        return !userPrincipal.matchesUser(dbUser);
    }

    /**
     * Creates a fresh UserPrincipal from the database for the given user.
     *
     * @param user The user entity
     * @param authenticationType The authentication type
     * @return A fresh UserPrincipal
     */
    public UserPrincipal refreshPrincipal(User user, com.ehb.connected.domain.impl.auth.entities.AuthenticationType authenticationType) {
        return UserPrincipal.fromUser(user, authenticationType);
    }
}
