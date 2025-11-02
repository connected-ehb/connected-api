package com.ehb.connected.domain.impl.auth.services;

import com.ehb.connected.domain.impl.auth.entities.LoginRequestDto;
import com.ehb.connected.domain.impl.auth.entities.RegistrationRequestDto;
import com.ehb.connected.domain.impl.users.dto.AuthUserDetailsDto;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

public interface AuthService {
    UserDetailsDto register(RegistrationRequestDto request);
    UserDetailsDto login(LoginRequestDto request);
    void logout(Authentication authentication, HttpServletResponse response);
    AuthUserDetailsDto getCurrentUser(HttpServletRequest request);
    AuthUserDetailsDto refreshSessionIfStale(HttpServletRequest request);
}
