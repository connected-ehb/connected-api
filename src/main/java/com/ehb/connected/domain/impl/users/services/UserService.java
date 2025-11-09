package com.ehb.connected.domain.impl.users.services;

import com.ehb.connected.domain.impl.users.dto.EmailRequestDto;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import com.ehb.connected.domain.impl.users.dto.AuthUserDetailsDto;
import com.ehb.connected.domain.impl.users.entities.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;

public interface UserService {
    List<UserDetailsDto> getAllStudentsByCourseId(Long courseId);
    User getUserById(Long id);
    User createUser(User user);
    UserDetailsDto updateUser(Authentication authentication, UserDetailsDto user);
    UserDetailsDto updateUserAsAdmin(Long userId, UserDetailsDto userDetailsDto);
    void deleteUser(Long id);

    User getUserByAuthentication(Authentication authentication);

    AuthUserDetailsDto getCurrentUser(OAuth2User principal);
    void requestDeleteUser(Authentication authentication);
    void createEmailVerificationTokenByCanvasId(OAuth2User principal, EmailRequestDto emailRequestDto);
    void verifyEmailToken(String token);
}
