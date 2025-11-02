package com.ehb.connected.domain.impl.auth.services;

import com.ehb.connected.domain.impl.auth.models.RememberMeToken;
import com.ehb.connected.domain.impl.auth.repositories.RememberMeTokenRepository;
import com.ehb.connected.domain.impl.users.entities.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing remember-me authentication tokens.
 * <p>
 * Security Implementation:
 * - Tokens are generated with SecureRandom (256-bit)
 * - Tokens are hashed with BCrypt before database storage
 * - Cookie is HttpOnly and Secure
 * - Tokens expire after 30 days
 * - Each user can have only one active token
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RememberMeServiceImpl implements RememberMeService {

    private static final String COOKIE_NAME = "CONNECTED_REMEMBER_ME";
    private static final int TOKEN_VALIDITY_DAYS = 30;
    private static final int TOKEN_BYTES = 32; // 256 bits

    private final RememberMeTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a new remember-me token for the user.
     * Deletes any existing tokens and generates a new one.
     */
    @Override
    @Transactional
    public void createRememberMeToken(User user, HttpServletResponse response) {
        // Generate secure random token with user ID embedded
        String rawToken = generateSecureToken(user.getId());

        // Hash it with BCrypt (like a password)
        String tokenHash = passwordEncoder.encode(rawToken);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = now.plusDays(TOKEN_VALIDITY_DAYS);

        // Delete old tokens (only one token per user)
        tokenRepository.deleteByUserId(user.getId());

        // Save hashed token to database
        tokenRepository.save(RememberMeToken.builder()
                .tokenHash(tokenHash)
                .user(user)
                .createdAt(now)
                .expiresAt(expiry)
                .build());

        // Set cookie with raw token (not the hash!)
        Cookie cookie = createCookie(COOKIE_NAME, rawToken, TOKEN_VALIDITY_DAYS * 24 * 60 * 60);
        response.addCookie(cookie);

        log.debug("Created remember-me token for user {}", user.getId());
    }

    /**
     * Validates a remember-me token from the request cookie.
     * Returns the user if token is valid, empty otherwise.
     */
    @Override
    public Optional<User> validateRememberMeToken(HttpServletRequest request) {
        // Extract token from cookie
        String rawToken = extractTokenFromCookie(request);
        if (rawToken == null) {
            return Optional.empty();
        }

        // We can't look up by token (it's hashed), so we need to extract user ID
        // from the token format: userId:randomBytes (base64 encoded)
        String[] parts = decodeToken(rawToken);
        if (parts == null) {
            log.warn("Invalid remember-me token format");
            return Optional.empty();
        }

        Long userId = Long.parseLong(parts[0]);

        // Find all tokens for this user
        List<RememberMeToken> userTokens = tokenRepository.findByUserId(userId);

        // Check if any token hash matches (using BCrypt)
        for (RememberMeToken token : userTokens) {
            if (!token.isExpired() && passwordEncoder.matches(rawToken, token.getTokenHash())) {
                log.debug("Valid remember-me token for user {}", userId);
                return Optional.of(token.getUser());
            }
        }

        log.debug("No valid remember-me token found for user {}", userId);
        return Optional.empty();
    }

    /**
     * Clears the remember-me token for a user.
     * Called on logout or security events (password change, etc.).
     */
    @Override
    @Transactional
    public void clearRememberMe(User user, HttpServletResponse response) {
        tokenRepository.deleteByUserId(user.getId());

        // Clear cookie
        Cookie cookie = createCookie(COOKIE_NAME, null, 0);
        response.addCookie(cookie);

        log.debug("Cleared remember-me token for user {}", user.getId());
    }

    /**
     * Generates a secure random token with embedded user ID.
     * Format: userId:randomBytes (base64 encoded)
     * This allows us to look up the user's tokens in the database efficiently.
     */
    private String generateSecureToken(Long userId) {
        byte[] randomBytes = new byte[TOKEN_BYTES];
        new SecureRandom().nextBytes(randomBytes);
        String randomPart = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        // Combine user ID with random bytes: "userId:randomString"
        String combined = userId + ":" + randomPart;

        // Base64 encode the whole thing for cookie safety
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(combined.getBytes());
    }

    /**
     * Extracts the raw token from the request cookie.
     */
    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * Decodes a token to extract user ID and random part.
     * Returns null if token format is invalid.
     */
    private String[] decodeToken(String token) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(token));
            return decoded.split(":", 2);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Creates a cookie with standard security settings.
     */
    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        return cookie;
    }
}