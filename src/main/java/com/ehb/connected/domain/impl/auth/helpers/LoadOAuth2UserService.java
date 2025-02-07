package com.ehb.connected.domain.impl.auth.helpers;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class LoadOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final Logger logger = LoggerFactory.getLogger(LoadOAuth2UserService.class);
    private final WebClient webClient = WebClient.builder().build();

    // This method overrides the default loadUser method to fetch user attributes from the user info endpoint.
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Get the URI of the user info endpoint
        String userInfoUri = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUri();

        // Use the access token to fetch the user info manually
        String accessToken = userRequest.getAccessToken().getTokenValue();
        Map<String, Object> attributes = webClient.get()
                .uri(userInfoUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        // Log the full raw response
        logger.info("Received raw user attributes from Canvas: {}", attributes);

        // If the "email" attribute is missing, try fetching it using an admin token.
        //this is an ugly "hack" to get the email from the user if it is not present in the response.
        //TODO: find a better way to get the email from the user
        if (attributes.get("email") == null) {
            try {
                // Admin token (permanent access token)
                String adminToken = "c29UXcGKkyxWuLDruYMe4u73Y788NKy3UyGEaEWH3x26kfvkJRNf3UETc7u8nzEk";
                // Endpoint to fetch users with the email included
                String fallbackUri = "https://canvas.mertenshome.com/api/v1/accounts/self/users?include[]=email";
                List<Map<String, Object>> users = webClient.get()
                        .uri(fallbackUri)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                        .block();
                // Retrieve the current user's id from the original response
                Object currentUserId = attributes.get("id");
                if (currentUserId != null && users != null) {
                    for (Map<String, Object> user : users) {
                        // Compare the "id" as a string to be safe
                        if (user.get("id") != null &&
                                user.get("id").toString().equals(currentUserId.toString())) {
                            // If found and email is present, add it to attributes
                            if (user.get("email") != null) {
                                attributes.put("email", user.get("email"));
                                logger.info("Patched email from fallback request: {}", user.get("email"));
                            }
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error fetching fallback email using admin token", e);
            }
        }

        String userNameAttributeKey = "email";

        // Create a default authority (you can customize this if needed)
        // Typically, the authorities are determined by your OAuth2 provider or your app's logic.
        // Here we simply grant ROLE_USER.
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_STUDENT")),
                attributes,
                userNameAttributeKey
        );
    }
}

