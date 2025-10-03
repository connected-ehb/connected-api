package com.ehb.connected.domain.impl.auth.services;

import com.ehb.connected.domain.impl.auth.entities.LoginRequestDto;
import com.ehb.connected.domain.impl.auth.entities.RegistrationRequestDto;
import com.ehb.connected.domain.impl.users.dto.AuthUserDetailsDto;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.security.Principal;

public interface AuthService {
    UserDetailsDto register(RegistrationRequestDto request);
    UserDetailsDto login(LoginRequestDto request);
    void logout(Principal principal);
    AuthUserDetailsDto refreshSessionIfStale(HttpServletRequest request);
}
