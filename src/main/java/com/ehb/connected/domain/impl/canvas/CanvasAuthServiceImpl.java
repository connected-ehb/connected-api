package com.ehb.connected.domain.impl.canvas;

import com.ehb.connected.domain.impl.users.entities.Role;
import com.ehb.connected.exceptions.BaseRuntimeException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CanvasAuthServiceImpl implements CanvasAuthService {

    private final WebClient webClient;

    @Override
    public void deleteAccessToken(String accessToken) {
        webClient.delete()
                .uri("https://canvas.mertenshome.com/login/oauth2/token")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
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
    public String refreshAccessToken() {
        return null;
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
