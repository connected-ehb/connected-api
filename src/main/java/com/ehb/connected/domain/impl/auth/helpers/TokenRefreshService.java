package com.ehb.connected.domain.impl.auth.helpers;

import com.ehb.connected.domain.impl.canvas.CanvasAuthService;
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
    private final CanvasAuthService canvasAuthService;
    private final UserRepository userRepository;

    /**
     * Refreshes the access token for a user if it's expired or about to expire
     * @param authentication The OAuth2 authentication token
     * @return The new access token, or null if no refresh was needed
     */
    public String refreshTokenIfNeeded(OAuth2AuthenticationToken authentication) {
        if (authentication == null) {
            log.debug("No OAuth2 authentication present");
            return null;
        }

        String registrationId = authentication.getAuthorizedClientRegistrationId();
        String principalName = authentication.getName();

        log.debug("Checking token for principal: {}", principalName);

        // Build an authorize request to check and refresh the token if needed
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
        Instant expiresAt = accessToken.getExpiresAt();
        
        if (expiresAt == null) {
            log.warn("Access token expiry time is null for principal: {}", principalName);
            return null;
        }

        Instant now = Instant.now();
        long secondsToExpiry = expiresAt.getEpochSecond() - now.getEpochSecond();
        
        log.debug("Access token for principal: {} expires in {} seconds.", principalName, secondsToExpiry);

        // Refresh if token expires in less than 5 minutes
        if (secondsToExpiry < 300) {
            log.info("Access token is expired for principal: {}. Refreshing token.", principalName);
            String newAccessToken = canvasAuthService.refreshAccessToken(authorizedClient);

            // Update the user's access token in the database
            try {
                User user = userRepository.findByCanvasUserId(Long.parseLong(principalName))
                        .orElseThrow(() -> new EntityNotFoundException("User not found with canvasId: " + principalName));
                user.setAccessToken(newAccessToken);
                userRepository.save(user);
                log.info("Updated access token for user: {}", principalName);
                return newAccessToken;
            } catch (Exception e) {
                log.error("Failed to update access token for user: {}", principalName, e);
                return null;
            }
        } else {
            log.debug("Access token for principal: {} is still valid.", principalName);
            return null;
        }
    }
}
    