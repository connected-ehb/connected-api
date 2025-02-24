package com.ehb.connected.domain.impl.canvas;

import com.ehb.connected.domain.impl.users.entities.Role;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;

import java.util.Map;

public interface CanvasAuthService {

    void deleteAccessToken(String accessToken);

    String getAccessToken(OAuth2UserRequest userRequest);

    Map<String, Object> getUserInfo(String url, String accessToken);

    Object getNonAdminUserEmail(Map<String, Object> attributes);

    String refreshAccessToken();

    String getUserInfoUri(OAuth2UserRequest userRequest);

    Role determineRoleByEmail(String email);
}
