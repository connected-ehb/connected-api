package com.ehb.connected.domain.impl.auth.helpers;

import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CanvasTokenService {

    private final TokenRefreshService tokenRefreshService;
    private final UserService userService;

    /**
     * Gets a valid access token for the current user, refreshing if necessary
     * @param principal The security principal
     * @return The valid access token
     */
    public String getValidAccessToken(Principal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Principal cannot be null");
        }

        // First, try to refresh the token if needed
        OAuth2AuthenticationToken oauth2Token = getCurrentOAuth2Authentication();
        if (oauth2Token != null) {
            String refreshedToken = tokenRefreshService.refreshTokenIfNeeded(oauth2Token);
            if (refreshedToken != null) {
                log.debug("Token refreshed successfully for user: {}", principal.getName());
                return refreshedToken;
            }
        }

        // If no refresh was needed or no OAuth2 token, get the token from the database
        User user = userService.getUserFromAnyPrincipal(principal);
        String accessToken = user.getAccessToken();
        
        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new IllegalStateException("No access token found for user: " + principal.getName());
        }

        return accessToken;
    }

    /**
     * Gets the current OAuth2AuthenticationToken from SecurityContext
     */
    private OAuth2AuthenticationToken getCurrentOAuth2Authentication() {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof OAuth2AuthenticationToken) {
                return (OAuth2AuthenticationToken) authentication;
            }
        } catch (Exception e) {
            log.debug("Could not retrieve OAuth2 authentication from SecurityContext", e);
        }
        return null;
    }
}
