package com.ehb.connected.domain.impl.auth.helpers;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Component
public class TokenRefreshFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(TokenRefreshFilter.class);
    private final OAuth2AuthorizedClientManager authorizedClientManager;

    public TokenRefreshFilter(OAuth2AuthorizedClientManager authorizedClientManager) {
        this.authorizedClientManager = authorizedClientManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        logger.info("[TokenFilter] TokenRefreshFilter triggered for request: {}", request.getRequestURI());

        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();

        if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
            String registrationId = oauth2Token.getAuthorizedClientRegistrationId();
            String principalName = oauth2Token.getName();

            logger.info("[TokenFilter] OAuth2 authentication found. RegistrationId: {}, Principal: {}",
                    registrationId, principalName);

            // Build an authorize request to check and refresh the token if needed.
            OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                    .withClientRegistrationId(registrationId)
                    .principal(oauth2Token)
                    .build();

            // This call refreshes the token if it's near expiry.
            OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

            if (authorizedClient == null) {
                logger.error("[TokenFilter] Authorization failed for principal: {}. Authorized client is null.", principalName);
            } else {
                Instant expiresAt = authorizedClient.getAccessToken().getExpiresAt();
                if (expiresAt != null) {
                    long secondsToExpiry = expiresAt.getEpochSecond() - Instant.now().getEpochSecond();
                    logger.info("[TokenFilter] Access token for principal: {} expires in {} seconds.", principalName, secondsToExpiry);

                    if (secondsToExpiry < 300) { // less than 5 minutes to expiry
                        logger.info("[TokenFilter] Access token is within 5 minutes of expiry for principal: {}. Token refreshed (if necessary).", principalName);

                    } else {
                        logger.debug("[TokenFilter] Access token for principal: {} is still valid.", principalName);
                    }
                } else {
                    logger.warn("[TokenFilter] Access token expiry time is null for principal: {}", principalName);
                }
            }
        } else {
            logger.debug("[TokenFilter] No OAuth2 authentication present. Skipping token refresh filter.");
        }

        filterChain.doFilter(request, response);
    }
}
