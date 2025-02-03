package com.ehb.connected.domain.impl.auth.controllers;

import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.mappers.UserDetailsMapper;
import com.ehb.connected.domain.impl.users.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserDetailsMapper userDetailsMapper;

    @GetMapping("/user")
    public UserDetailsDto getCurrentUser(@AuthenticationPrincipal OAuth2User principal) {
        User user = userService.getUserByEmail(principal.getAttribute("email"));
        UserDetailsDto userDetailsDto = userDetailsMapper.toDtoWithPrincipal(user, principal);
        return userDetailsDto;
    }
}
