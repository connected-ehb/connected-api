package com.ehb.connected.domain.impl.auth.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");

        Map<String, Object> responseData = new HashMap<>();
        
        if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
            // OAuth2 authentication
            OAuth2User oauth2User = oauth2Token.getPrincipal();
            String canvasUserId = oauth2User.getName();
            
            // Find the user in our database
            User user = userRepository.findByCanvasUserId(Long.parseLong(canvasUserId))
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            responseData.put("message", "OAuth2 login successful");
            responseData.put("canvasUserId", canvasUserId);
            responseData.put("emailVerified", user.isEmailVerified());
            responseData.put("role", user.getRole());
            responseData.put("firstName", user.getFirstName());
            responseData.put("lastName", user.getLastName());
            
            if (!user.isEmailVerified()) {
                responseData.put("requiresEmailVerification", true);
            }
        } else {
            // Form-based authentication
            responseData.put("message", "Login successful");
            responseData.put("username", authentication.getName());
        }

        response.getWriter().write(objectMapper.writeValueAsString(responseData));
    }
}
