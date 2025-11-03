package com.ehb.connected.domain.impl.auth.services;

import com.ehb.connected.domain.impl.auth.dto.LoginRequest;
import com.ehb.connected.domain.impl.auth.dto.RegistrationRequest;
import com.ehb.connected.domain.impl.users.dto.AuthUserDetailsDto;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

/**
 * Service interface for authentication operations.
 * Handles user registration, login, logout, and session management.
 */
public interface AuthService {
    UserDetailsDto register(RegistrationRequest request);
    UserDetailsDto login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse);
    void logout(Authentication authentication, HttpServletResponse response);
    AuthUserDetailsDto getCurrentUser(HttpServletRequest request);
    AuthUserDetailsDto refreshSessionIfStale(HttpServletRequest request);
}
