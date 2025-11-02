package com.ehb.connected.domain.impl.auth.controllers;

import com.ehb.connected.domain.impl.auth.dto.LoginRequest;
import com.ehb.connected.domain.impl.auth.dto.RegistrationRequest;
import com.ehb.connected.domain.impl.auth.services.AuthService;
import com.ehb.connected.domain.impl.users.dto.AuthUserDetailsDto;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints.
 * Handles user registration, login, and session retrieval.
 * Logout is handled by Spring Security's logout filter (see SecurityConfig).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Get the current authenticated user's details.
     * Automatically refreshes stale session data if needed.
     */
    @GetMapping("/user")
    public ResponseEntity<AuthUserDetailsDto> getCurrentUser(HttpServletRequest request) {
        return ResponseEntity.ok(authService.getCurrentUser(request));
    }

    /**
     * Authenticate a user with email and password (form-based login).
     * Note: This currently only validates credentials but doesn't create a session.
     * TODO: Fix to properly integrate with Spring Security.
     */
    @PostMapping("/login")
    public ResponseEntity<UserDetailsDto> loginUser(@RequestBody LoginRequest request) {
        UserDetailsDto userDetails = authService.login(request);
        return ResponseEntity.ok(userDetails);
    }

    /**
     * Register a new researcher user (invite-only).
     * Requires a valid invitation code.
     */
    @PostMapping("/register")
    public ResponseEntity<UserDetailsDto> registerUser(@RequestBody RegistrationRequest request) {
        UserDetailsDto registeredUser = authService.register(request);
        return ResponseEntity.ok(registeredUser);
    }
}
