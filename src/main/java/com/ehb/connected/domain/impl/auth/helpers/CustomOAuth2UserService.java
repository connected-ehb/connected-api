package com.ehb.connected.domain.impl.auth.helpers;

import java.util.Map;

import com.ehb.connected.domain.impl.canvas.CanvasAuthService;
import com.ehb.connected.domain.impl.users.entities.Role;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);
    private final UserRepository userRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;

    private final CanvasAuthService canvasAuthService;

    public CustomOAuth2UserService(UserRepository userRepository,
                                   OAuth2AuthorizedClientService authorizedClientService,
                                   CanvasAuthService canvasAuthService
    ) {
        this.userRepository = userRepository;
        this.authorizedClientService = authorizedClientService;
        this.canvasAuthService = canvasAuthService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. Retrieve user info endpoint URI and access token.
        String userInfoUri = canvasAuthService.getUserInfoUri(userRequest);
        String accessToken = canvasAuthService.getAccessToken(userRequest);

        // 2. Fetch user attributes.
        Map<String, Object> attributes = canvasAuthService.getUserInfo(userInfoUri, accessToken);

        logger.info("Received user attributes: {}", attributes);

        // 3. Use canvasUserId to find the user
        Long canvasUserId = Long.parseLong(attributes.get("id").toString());
        User user = userRepository.findByCanvasUserId(canvasUserId).orElse(new User());

        if(user.getAccessToken() != null && !user.getAccessToken().equals(accessToken)) {
            canvasAuthService.deleteAccessToken(user.getAccessToken());
        }

        // When a user logs in for the first time, email and role will be null.
        // These will be set after the user provides them and verifies their email.
        if (user.getId() == null) { // New user
            user.setEmail(null);
            user.setRole(null);
            user.setEmailVerified(false);
        }


        user.setAccessToken(accessToken);
        user.setAttributes(attributes);
        user.setCanvasUserId(Long.parseLong(attributes.get("id").toString()));
        user.setFirstName((String) attributes.get("first_name"));
        user.setLastName((String) attributes.get("last_name"));
        user.setProfileImageUrl((String) attributes.get("avatar_url"));

        // 6. Retrieve refresh token via the OAuth2AuthorizedClientService.
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // The principal name is what Spring Security uses to identify the user.
        // It's derived from the 'user-name-attribute' in your application.yml, which should be set to 'id'.
        // We use the canvasUserId we fetched from the attributes as the principal name.
        String principalName = String.valueOf(canvasUserId);
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(registrationId, principalName);

        if (authorizedClient != null && authorizedClient.getRefreshToken() != null) {
            user.setRefreshToken(authorizedClient.getRefreshToken().getTokenValue());
        }

        // 7. Persist the user.
        userRepository.save(user);

        // 8. Return the User instance directly.
        return user;
    }
}
