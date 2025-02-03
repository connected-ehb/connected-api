package com.ehb.connected.domain.impl.auth.helpers;

import com.ehb.connected.domain.impl.users.entities.Role;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final UserRepository userRepository; // Your repository for persisting users

    public OAuth2LoginSuccessHandler(OAuth2AuthorizedClientService authorizedClientService,
                                     UserRepository userRepository) {
        this.authorizedClientService = authorizedClientService;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
            String registrationId = oauth2Token.getAuthorizedClientRegistrationId();
            String principalName = oauth2Token.getName();

            // Retrieve the authorized client which contains the access token.
            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                    registrationId, principalName);

            String accessToken = authorizedClient.getAccessToken().getTokenValue();
            if (authorizedClient.getRefreshToken() != null) {
                String refreshToken = authorizedClient.getRefreshToken().getTokenValue();
            }

            // Retrieve user attributes from the authentication principal
            var attributes = oauth2Token.getPrincipal().getAttributes();
            String firstName = (String) attributes.get("first_name");
            String email = (String) attributes.get("email");
            Role role = email.endsWith("@ehb.be") ? Role.TEACHER : Role.STUDENT;

            // Persist or update your user record in your own database.
            User user = userRepository.findByEmail(email)
                    .orElse(new User());
            user.setFirstName(firstName);
            user.setEmail(email);
            user.setRole(role);
            user.setAccessToken(accessToken);
            // ... set additional fields as needed

            userRepository.save(user);
        }

        // Redirect to your post-login page
        response.sendRedirect("http://localhost:4200");
    }
}
