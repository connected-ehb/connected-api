package com.ehb.connected.domain.impl.auth.helpers;

import java.util.List;
import java.util.Map;

import com.ehb.connected.domain.impl.users.entities.Role;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);
    private final UserRepository userRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final WebClient webClient = WebClient.builder().build();

    public CustomOAuth2UserService(UserRepository userRepository,
                                   OAuth2AuthorizedClientService authorizedClientService) {
        this.userRepository = userRepository;
        this.authorizedClientService = authorizedClientService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. Retrieve user info endpoint URI and access token.
        String userInfoUri = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUri();
        String accessToken = userRequest.getAccessToken().getTokenValue();

        // 2. Fetch user attributes.
        Map<String, Object> attributes = webClient.get()
                .uri(userInfoUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        logger.info("Received user attributes: {}", attributes);

        // 3. Fallback logic if email is missing.
        if (attributes.get("email") == null) {
            try {
                // Admin token (permanent access token)
                String adminToken = "c29UXcGKkyxWuLDruYMe4u73Y788NKy3UyGEaEWH3x26kfvkJRNf3UETc7u8nzEk";
                // Retrieve the current user's id from the original response
                Object currentUserIdObj = attributes.get("id");
                if (currentUserIdObj != null) {
                    String currentUserId = currentUserIdObj.toString();
                    // Endpoint to fetch a specific user's details
                    String fallbackUri = "https://canvas.mertenshome.com/api/v1/users/" + currentUserId;
                    Map<String, Object> userDetail = webClient.get()
                            .uri(fallbackUri)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                            .block();
                    if (userDetail != null && userDetail.get("email") != null) {
                        attributes.put("email", userDetail.get("email"));
                        logger.info("Patched email from fallback request: {}", userDetail.get("email"));
                    }
                }
            } catch (Exception e) {
                logger.error("Error fetching fallback email using admin token", e);
            }
        }

        // 4. Extract email and determine role.
        String email = attributes.get("email").toString();
        Role role = email.endsWith("@ehb.be") ? Role.TEACHER : Role.STUDENT;

        // 5. Load or create the local user entity.
        User user = userRepository.findByEmail(email).orElse(new User());
        user.setEmail(email);
        user.setRole(role);
        user.setAccessToken(accessToken);
        user.setAttributes(attributes);
        user.setCanvasUserId(Long.parseLong(attributes.get("id").toString()));
        user.setFirstName((String) attributes.get("first_name"));
        user.setLastName((String) attributes.get("last_name"));
        user.setProfileImageUrl((String) attributes.get("avatar_url"));

        // 6. Retrieve refresh token via the OAuth2AuthorizedClientService.
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(registrationId, email);
        if (authorizedClient != null && authorizedClient.getRefreshToken() != null) {
            user.setRefreshToken(authorizedClient.getRefreshToken().getTokenValue());
        }

        // 7. Persist the user.
        userRepository.save(user);

        // 8. Return the User instance directly.
        return user;
    }
}

