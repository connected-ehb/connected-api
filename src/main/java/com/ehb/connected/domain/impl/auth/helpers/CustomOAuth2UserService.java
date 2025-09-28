package com.ehb.connected.domain.impl.auth.helpers;

import com.ehb.connected.domain.impl.canvas.CanvasAuthService;
import com.ehb.connected.domain.impl.canvas.entities.CanvasAttributes;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final CanvasAuthService canvasAuthService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // First, get the OAuth2 user from the default service
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        // Get the attributes that Spring Security already fetched
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        log.info("Received Canvas user attributes from OAuth2: {}", attributes);
        
        // Convert to strongly typed CanvasAttributes
        CanvasAttributes canvasAttributes = CanvasAttributes.fromOAuth2Attributes(attributes);
        
        // Find or create user using the typed attributes
        User user = userRepository.findByCanvasUserId(canvasAttributes.getId())
                .orElseGet(() -> createNewUser(canvasAttributes));
        
        // Update tokens
        updateUserTokens(user, userRequest);

        // TODO update all user attributes in database
        
        // Save user
        user = userRepository.save(user);
        
        // Create OAuth2 user with our User entity as principal
        return new DefaultOAuth2User(
                user.getAuthorities(),
                attributes,
                "id" // name attribute key
        );
    }
    
    private User createNewUser(CanvasAttributes canvasAttributes) {
        User user = new User();
        user.setCanvasUserId(canvasAttributes.getId());
        user.setFirstName(canvasAttributes.getFirstName());
        user.setLastName(canvasAttributes.getLastName());
        user.setProfileImageUrl(canvasAttributes.getProfileImageUrl());
        user.setEmail(null); // Will be set during email verification
        user.setRole(null); // Will be set during email verification
        user.setEmailVerified(false);
        user.setEnabled(true);
        
        log.info("Created new user from Canvas attributes: {} {} (ID: {})", 
                canvasAttributes.getFirstName(), 
                canvasAttributes.getLastName(), 
                canvasAttributes.getId());
        
        return user;
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
}
