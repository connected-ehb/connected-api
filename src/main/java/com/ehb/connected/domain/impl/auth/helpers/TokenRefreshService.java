package com.ehb.connected.domain.impl.auth.helpers;

import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.repositories.UserRepository;
import com.ehb.connected.exceptions.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenRefreshService {

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final UserRepository userRepository;

    /**
     * Ensures the access token for a user is valid.
     * Uses OAuth2AuthorizedClientManager (which refreshes if needed) and stores new value if it changed.
     * Returns the token only when updated so callers can override the Authorization header.
     */
    public String refreshTokenIfNeeded(OAuth2AuthenticationToken authentication) {
        if (authentication == null) {
            log.debug("No OAuth2 authentication present");
            return null;
        }

        String registrationId = authentication.getAuthorizedClientRegistrationId();
        String principalName = authentication.getName();

        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(registrationId)
                .principal(authentication)
                .build();

        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);
        if (authorizedClient == null) {
            log.error("Authorization failed for principal: {}. Authorized client is null.", principalName);
            return null;
        }

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        if (accessToken == null) {
            log.warn("No access token available for principal: {}", principalName);
            return null;
        }

        String tokenValue = accessToken.getTokenValue();
        Instant expiresAt = accessToken.getExpiresAt();
        long secondsToExpiry = expiresAt != null ? (expiresAt.getEpochSecond() - Instant.now().getEpochSecond()) : -1;
        log.debug("Access token for principal: {} expires in {} seconds.", principalName, secondsToExpiry);

        try {
            User user = userRepository.findByCanvasUserId(Long.parseLong(principalName))
                    .orElseThrow(() -> new EntityNotFoundException("User not found with canvasId: " + principalName));

            if (user.getAccessToken() == null || !user.getAccessToken().equals(tokenValue)) {
                user.setAccessToken(tokenValue);
                userRepository.save(user);
                log.info("Updated access token for user: {}", principalName);
                return tokenValue;
            }
        } catch (Exception e) {
            log.error("Failed to update access token for user: {}", principalName, e);
        }

        return null;
    }
}
