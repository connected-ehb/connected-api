package com.ehb.connected.domain.impl.auth.controllers;

import com.ehb.connected.domain.impl.auth.entities.LoginRequestDto;
import com.ehb.connected.domain.impl.auth.entities.RegistrationRequestDto;
import com.ehb.connected.domain.impl.auth.services.AuthService;
import com.ehb.connected.domain.impl.users.dto.AuthUserDetailsDto;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @GetMapping("/user")
    public ResponseEntity<AuthUserDetailsDto> getCurrentUser(HttpServletRequest request) {
        return ResponseEntity.ok(authService.getCurrentUser(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Principal principal) {
        authService.logout(principal);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<UserDetailsDto> loginUser(@RequestBody LoginRequestDto request) {
        UserDetailsDto userDetails = authService.login(request);
        return ResponseEntity.ok(userDetails);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDetailsDto> registerUser(@RequestBody RegistrationRequestDto request) {
        UserDetailsDto registeredUser = authService.register(request);
        return ResponseEntity.ok(registeredUser);
    }
}
