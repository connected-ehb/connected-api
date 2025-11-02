package com.ehb.connected.domain.impl.auth.handlers;

import com.ehb.connected.domain.impl.auth.services.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom logout success handler that delegates to AuthService for cleanup operations.
 * Called by Spring Security after logout filter completes session invalidation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final AuthService authService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {

        log.debug("Logout success handler invoked for authentication: {}",
                authentication != null ? authentication.getName() : "null");

        // Delegate to AuthService for all logout cleanup operations
        // (OAuth2 token revocation, remember-me cleanup, etc.)
        authService.logout(authentication, response);
    }
}