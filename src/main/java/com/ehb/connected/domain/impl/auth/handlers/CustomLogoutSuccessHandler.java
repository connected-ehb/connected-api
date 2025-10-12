package com.ehb.connected.domain.impl.auth.handlers;

import com.ehb.connected.domain.impl.auth.services.RememberMeService;
import com.ehb.connected.domain.impl.canvas.CanvasAuthService;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.repositories.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final UserRepository userRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final CanvasAuthService canvasAuthService;
    private final RememberMeService rememberMeService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {

        if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
            String registrationId = oauth2Token.getAuthorizedClientRegistrationId();
            String principalName = oauth2Token.getName();

            // Load the authorized client from Spring's storage
            OAuth2AuthorizedClient authorizedClient =
                    authorizedClientService.loadAuthorizedClient(registrationId, principalName);

            if (authorizedClient != null) {
                String accessToken = authorizedClient.getAccessToken().getTokenValue();

                // Delete/revoke token on Canvas
                try {
                    canvasAuthService.deleteAccessToken(accessToken);
                    log.info("Revoked Canvas access token for user: {}", principalName);
                } catch (Exception e) {
                    log.error("Failed to revoke Canvas token for user: {}", principalName, e);
                }

                // Remove tokens from Spring's storage
                authorizedClientService.removeAuthorizedClient(registrationId, principalName);
                log.info("Removed OAuth2 authorized client for user: {}", principalName);
            }

            // Clear remember-me token
            Optional<User> optionalUser = userRepository.findByCanvasUserId(Long.parseLong(principalName));
            optionalUser.ifPresent(user -> rememberMeService.clearRememberMe(user, response));
        }
    }
}