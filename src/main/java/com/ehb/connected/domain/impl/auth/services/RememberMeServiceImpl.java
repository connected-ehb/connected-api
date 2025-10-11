package com.ehb.connected.domain.impl.auth.services;

import com.ehb.connected.domain.impl.auth.entities.RememberMeToken;
import com.ehb.connected.domain.impl.auth.repositories.RememberMeTokenRepository;
import com.ehb.connected.domain.impl.users.entities.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RememberMeServiceImpl implements RememberMeService {

    private static final String COOKIE_NAME = "CONNECTED_REMEMBER_ME";
    private static final int TOKEN_VALIDITY_DAYS = 30;

    private final RememberMeTokenRepository rememberMeTokenRepository;

    @Transactional
    public void createRememberMeToken(User user, HttpServletResponse response) {
        String rawToken = generateSecureToken();
        LocalDateTime expiry = LocalDateTime.now().plusDays(TOKEN_VALIDITY_DAYS);

        rememberMeTokenRepository.deleteByUserId(user.getId());
        rememberMeTokenRepository.save(RememberMeToken.builder()
                .token(rawToken)
                .user(user)
                .createdAt(LocalDateTime.now())
                .expiresAt(expiry)
                .build());

        Cookie cookie = new Cookie(COOKIE_NAME, rawToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(TOKEN_VALIDITY_DAYS * 24 * 60 * 60);

        response.addCookie(cookie);
    }

    public Optional<User> validateRememberMeToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return Optional.empty();

        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                String token = cookie.getValue();
                return rememberMeTokenRepository.findByToken(token)
                        .filter(t -> !t.isExpired())
                        .map(RememberMeToken::getUser);
            }
        }
        return Optional.empty();
    }

    @Transactional
    public void clearRememberMe(User user, HttpServletResponse response) {
        rememberMeTokenRepository.deleteByUserId(user.getId());
        Cookie cookie = new Cookie(COOKIE_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
