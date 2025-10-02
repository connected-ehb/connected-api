package com.ehb.connected.domain.impl.auth.helpers;

import com.ehb.connected.domain.impl.canvas.CanvasAuthService;
import com.ehb.connected.domain.impl.canvas.entities.CanvasAttributes;
import com.ehb.connected.domain.impl.users.Factories.UserFactory;
import com.ehb.connected.domain.impl.users.entities.CustomOAuth2User;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final CanvasAuthService canvasAuthService;
    private final UserFactory userFactory;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Fetch user info from Canvas
        OAuth2User oauth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oauth2User.getAttributes();

        log.info("Received Canvas user attributes from OAuth2: {}", attributes);

        // Convert attributes to strongly typed object
        CanvasAttributes canvasAttributes = CanvasAttributes.fromOAuth2Attributes(attributes);

        // Find or create local user
        User user = userRepository.findByCanvasUserId(canvasAttributes.getId())
                .orElseGet(() -> userFactory.newCanvasUser(canvasAttributes));

        // Update tokens
        updateUserTokens(user, userRequest);

        // Sync user details from Canvas with local user
        syncCanvasAttributes(user, canvasAttributes);

        // Persist
        user = userRepository.save(user);

        String nameAttrKey = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        CustomOAuth2User customOAuth2User = new CustomOAuth2User(user, attributes, nameAttrKey);

        return customOAuth2User;
    }
    
    private void updateUserTokens(User user, OAuth2UserRequest userRequest) {
        // Get access token from the request
        String accessToken = userRequest.getAccessToken().getTokenValue();
        
        // Update access token
        if (user.getAccessToken() != null && !user.getAccessToken().equals(accessToken)) {
            canvasAuthService.deleteAccessToken(user.getAccessToken());
        }
        user.setAccessToken(accessToken);

        
        // Update refresh token
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String principalName = user.getCanvasUserId().toString();
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(registrationId, principalName);
        
        if (authorizedClient != null && authorizedClient.getRefreshToken() != null) {
            user.setRefreshToken(authorizedClient.getRefreshToken().getTokenValue());
        }
    }

    private void syncCanvasAttributes(User user, CanvasAttributes canvasAttributes) {
        boolean updated = false;

        if (!Objects.equals(canvasAttributes.getFirstName(), user.getFirstName())) {
            user.setFirstName(canvasAttributes.getFirstName());
            updated = true;
        }

        if (!Objects.equals(canvasAttributes.getLastName(), user.getLastName())) {
            user.setLastName(canvasAttributes.getLastName());
            updated = true;
        }

        if (!Objects.equals(canvasAttributes.getProfileImageUrl(), user.getProfileImageUrl())) {
            user.setProfileImageUrl(canvasAttributes.getProfileImageUrl());
            updated = true;
        }

        if (updated) {
            log.info("Updated user [{}] details from Canvas", user.getCanvasUserId());
        }
    }
}
