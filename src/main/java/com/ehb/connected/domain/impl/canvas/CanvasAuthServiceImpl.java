package com.ehb.connected.domain.impl.canvas;

import com.ehb.connected.domain.impl.users.entities.Role;
import com.ehb.connected.exceptions.BaseRuntimeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CanvasAuthServiceImpl implements CanvasAuthService {

    private final WebClient webClient;

    Logger logger = LoggerFactory.getLogger(CanvasAuthServiceImpl.class);

    @Override
    public void deleteAccessToken(String accessToken) {
        try {
            webClient.delete()
                    .uri("https://canvas.mertenshome.com/login/oauth2/token")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            logger.error("[{}] Failed to delete access token from Canvas", CanvasAuthService.class.getSimpleName());
        }
    }

    @Override
    public String getAccessToken(OAuth2UserRequest userRequest) {
        return userRequest.getAccessToken().getTokenValue();
    }

    @Override
    public Map<String, Object> getUserInfo(String url, String accessToken) {
        return webClient.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    }

    @Override
    public Object getNonAdminUserEmail(Map<String, Object> attributes) {
        String adminToken = "c29UXcGKkyxWuLDruYMe4u73Y788NKy3UyGEaEWH3x26kfvkJRNf3UETc7u8nzEk";
        String fallbackUri = "https://canvas.mertenshome.com/api/v1/users/" + attributes.get("id").toString();
        Map<String, Object> userDetail;
        try {
            userDetail = getUserInfo(fallbackUri, adminToken);
        } catch (Exception e) {
            throw new BaseRuntimeException("Failed to fetch user details from Canvas API", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return userDetail.get("email");
    }

    @Override
    public String refreshAccessToken(OAuth2AuthorizedClient authorizedClient) {
        Map<String, String> formData = getData(authorizedClient);

        String responseBody = webClient.post()
                .uri("https://canvas.mertenshome.com/login/oauth2/token")
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            return jsonNode.get("access_token").asText();
        } catch (JsonProcessingException e) {
            throw new BaseRuntimeException("Failed to parse JSON response", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @NotNull
    private static Map<String, String> getData(OAuth2AuthorizedClient authorizedClient) {
        String refreshToken = authorizedClient.getRefreshToken().getTokenValue();
        String clientId = authorizedClient.getClientRegistration().getClientId();
        String clientSecret = authorizedClient.getClientRegistration().getClientSecret();
        String redirectUri = authorizedClient.getClientRegistration().getRedirectUri();

        return Map.of(
                "grant_type", "refresh_token",
                "client_id", clientId,
                "client_secret", clientSecret,
                "refresh_token", refreshToken,
                "redirect_uri", redirectUri
        );
    }

    @Override
    public String getUserInfoUri(OAuth2UserRequest userRequest) {
        return userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUri();
    }

    @Override
    public Role determineRoleByEmail(String email) {
        return email.endsWith("@ehb.be") ? Role.TEACHER : Role.STUDENT;
    }
}
