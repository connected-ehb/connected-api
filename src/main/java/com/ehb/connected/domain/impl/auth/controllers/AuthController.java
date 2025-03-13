package com.ehb.connected.domain.impl.auth.controllers;

import com.ehb.connected.domain.impl.auth.entities.LoginRequestDto;
import com.ehb.connected.domain.impl.auth.entities.RegistrationRequestDto;
import com.ehb.connected.domain.impl.auth.services.AuthService;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import com.ehb.connected.domain.impl.users.dto.authUserDetailsDto;
import com.ehb.connected.domain.impl.users.entities.Role;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.mappers.UserDetailsMapper;
import com.ehb.connected.domain.impl.users.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserService userService;
    private final UserDetailsMapper userDetailsMapper;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/user")
    public authUserDetailsDto getCurrentUser(@AuthenticationPrincipal User principal) {
        User user = userService.getUserByEmail(principal.getEmail());
        return userDetailsMapper.toDtoWithPrincipal(user, principal);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Principal principal) {
        authService.logout(principal);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequestDto request) {
        UserDetailsDto userDetails = authService.login(request);
        return ResponseEntity.ok(userDetails);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegistrationRequestDto request) {
        UserDetailsDto registeredUser = authService.register(request);
        return ResponseEntity.ok(registeredUser);
    }
}
