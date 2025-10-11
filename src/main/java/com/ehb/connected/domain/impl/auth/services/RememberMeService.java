package com.ehb.connected.domain.impl.auth.services;

import com.ehb.connected.domain.impl.users.entities.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

public interface RememberMeService {
    void createRememberMeToken(User user, HttpServletResponse response);
    Optional<User> validateRememberMeToken(HttpServletRequest request);
    void clearRememberMe(User user, HttpServletResponse response);
}
