package com.ehb.connected.domain.impl.auth.handlers;

import com.ehb.connected.domain.impl.auth.services.RememberMeService;
import com.ehb.connected.domain.impl.auth.entities.CustomOAuth2User;
import com.ehb.connected.domain.impl.users.entities.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final RememberMeService rememberMeService;

    @Value("${connected.frontend-uri}")
    private String frontendUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");

        Map<String, Object> responseData = new HashMap<>();

        if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
            // OAuth2 authentication
            if (oauth2Token.getPrincipal() instanceof CustomOAuth2User customUser) {
                User user = customUser.getUser();

                // ✅ Create remember-me token and set cookie
                rememberMeService.createRememberMeToken(user, response);

                responseData.put("message", "OAuth2 login successful");
                responseData.put("canvasUserId", user.getCanvasUserId());
                responseData.put("emailVerified", user.isEmailVerified());
                responseData.put("role", user.getRole());
                responseData.put("firstName", user.getFirstName());
                responseData.put("lastName", user.getLastName());

                if (!user.isEmailVerified()) {
                    responseData.put("requiresEmailVerification", true);
                }

                // ✅ After creating the cookie, redirect browser to frontend
                String redirectUrl = frontendUri + "/login/success";
                log.info("OAuth2 login successful for user {}. Redirecting to {}", user.getFirstName() + " " + user.getLastName(), redirectUrl);
                response.sendRedirect(redirectUrl);
                return;

            } else {
                log.error("Expected CustomOAuth2User but got {}", oauth2Token.getPrincipal().getClass());
            }
        } else {
            responseData.put("message", "Login successful");
            responseData.put("username", authentication.getName());
        }

        // fallback: only if not redirected
        response.getWriter().write(objectMapper.writeValueAsString(responseData));
    }
}
