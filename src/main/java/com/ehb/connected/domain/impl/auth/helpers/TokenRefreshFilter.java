package com.ehb.connected.domain.impl.auth.helpers;

import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.repositories.UserRepository;
import com.ehb.connected.exceptions.EntityNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Component
public class TokenRefreshFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(TokenRefreshFilter.class);
    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final WebClient webClient;

    private final UserRepository userRepository;

    public TokenRefreshFilter(OAuth2AuthorizedClientManager authorizedClientManager, WebClient webClient, UserRepository userRepository) {
        this.authorizedClientManager = authorizedClientManager;
        this.webClient = webClient;
        this.userRepository = userRepository;
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

            OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

            if (authorizedClient == null) {
                logger.error("[TokenFilter] Authorization failed for principal: {}. Authorized client is null.", principalName);
            } else {
                Instant expiresAt = authorizedClient.getAccessToken().getExpiresAt();
                if (expiresAt != null) {
                    Instant now = Instant.now();
                    long secondsToExpiry = expiresAt.getEpochSecond() - now.getEpochSecond();
                    boolean isExpired = secondsToExpiry < 300;
                    logger.info("[TokenFilter] Access token for principal: {} expires in {} seconds.", principalName, secondsToExpiry);

                    if (isExpired) {
                        logger.info("[TokenFilter] Access token is expired for principal: {}. Token refreshed (if necessary).", principalName);
                        String refreshToken = authorizedClient.getRefreshToken().getTokenValue();
                        String clientId = authorizedClient.getClientRegistration().getClientId();
                        String clientSecret = authorizedClient.getClientRegistration().getClientSecret();
                        String redirectUri = authorizedClient.getClientRegistration().getRedirectUri();

                        Map<String, String> formData = Map.of(
                                "grant_type", "refresh_token",
                                "client_id", clientId,
                                "client_secret", clientSecret,
                                "refresh_token", refreshToken,
                                "redirect_uri", redirectUri
                        );

                        String responseBody = webClient.post()
                                .uri("/login/oauth2/token")
                                .bodyValue(formData)
                                .retrieve()
                                .bodyToMono(String.class)
                                .block();

                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode jsonNode = objectMapper.readTree(responseBody);
                        String newAccessToken = jsonNode.get("access_token").asText();

                        // Update the principal with the new access token
                        new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, newAccessToken, Instant.now(), expiresAt);
                        OAuth2AuthenticationToken newAuth = new OAuth2AuthenticationToken(oauth2Token.getPrincipal(), oauth2Token.getAuthorities(), oauth2Token.getAuthorizedClientRegistrationId());
                        SecurityContextHolder.getContext().setAuthentication(newAuth);

                        User user = userRepository.findByEmail(principalName).orElseThrow(() -> new EntityNotFoundException("User not found"));
                        user.setAccessToken(newAccessToken);
                        userRepository.save(user);

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
