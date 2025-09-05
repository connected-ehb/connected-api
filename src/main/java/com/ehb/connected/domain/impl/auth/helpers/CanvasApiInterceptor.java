package com.ehb.connected.domain.impl.auth.helpers;

import com.ehb.connected.domain.impl.users.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class CanvasApiInterceptor {

    private final TokenRefreshService tokenRefreshService;
    private final UserService userService;

    /**
     * Creates an ExchangeFilterFunction that automatically refreshes tokens before making Canvas API calls
     * @return ExchangeFilterFunction for token refresh
     */
    public ExchangeFilterFunction tokenRefreshFilter() {
        return new ExchangeFilterFunction() {
            @Override
            public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
                // Only apply token refresh for Canvas API calls
                if (isCanvasApiCall(request)) {
                    return refreshTokenAndProceed(request, next);
                }
                return next.exchange(request);
            }
        };
    }

    /**
     * Checks if the request is a Canvas API call
     */
    private boolean isCanvasApiCall(ClientRequest request) {
        String url = request.url().toString();
        return url.contains("/api/v1/") || url.contains("canvas.ehb.be");
    }

    /**
     * Refreshes the token if needed and proceeds with the request
     */
    private Mono<ClientResponse> refreshTokenAndProceed(ClientRequest request, ExchangeFunction next) {
        try {
            // Get the current authentication from SecurityContext
            OAuth2AuthenticationToken authentication = getCurrentOAuth2Authentication();
            
            if (authentication != null) {
                // Attempt to refresh the token if needed
                String newToken = tokenRefreshService.refreshTokenIfNeeded(authentication);
                
                if (newToken != null) {
                    log.debug("Token refreshed successfully, proceeding with Canvas API call");
                    // Create a new request with the refreshed token
                    ClientRequest updatedRequest = ClientRequest.from(request)
                            .header("Authorization", "Bearer " + newToken)
                            .build();
                    return next.exchange(updatedRequest);
                }
            }
            
            // If no refresh was needed or no authentication, proceed with original request
            return next.exchange(request);
            
        } catch (Exception e) {
            log.error("Error during token refresh for Canvas API call", e);
            // Proceed with original request even if refresh fails
            return next.exchange(request);
        }
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
