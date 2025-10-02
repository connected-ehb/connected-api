package com.ehb.connected.domain.impl.auth.helpers;

import com.ehb.connected.domain.impl.users.entities.CustomOAuth2User;
import com.ehb.connected.domain.impl.users.entities.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class AuthenticationRefresher {
    public void refreshIfStale(Authentication authentication, User dbUser, HttpServletRequest request) {
        if (!(authentication instanceof OAuth2AuthenticationToken oauth2Auth)) {
            return;
        }
        if (!(oauth2Auth.getPrincipal() instanceof CustomOAuth2User customUser)) {
            return;
        }

        User sessionUser = customUser.getUser();
        boolean roleChanged = !Objects.equals(dbUser.getRole(), sessionUser.getRole());
        boolean emailVerifiedChanged = dbUser.isEmailVerified() != sessionUser.isEmailVerified();

        if (roleChanged || emailVerifiedChanged) {
            log.info("Refreshing session Authentication for user {} (role/emailVerified changed)", dbUser.getId());

            CustomOAuth2User newPrincipal = new CustomOAuth2User(
                    dbUser,
                    customUser.getAttributes(),
                    customUser.getNameAttributeKey()
            );

            Authentication newAuth = new OAuth2AuthenticationToken(
                    newPrincipal,
                    newPrincipal.getAuthorities(),
                    oauth2Auth.getAuthorizedClientRegistrationId()
            );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(newAuth);
            SecurityContextHolder.setContext(context);

            request.getSession().setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    context
            );
        }
    }
}
