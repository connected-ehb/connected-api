package com.ehb.connected.domain.impl.auth.controllers;

import com.ehb.connected.domain.impl.auth.dto.LoginRequest;
import com.ehb.connected.domain.impl.auth.dto.RegistrationRequest;
import com.ehb.connected.domain.impl.auth.services.AuthService;
import com.ehb.connected.domain.impl.users.dto.AuthUserDetailsDto;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * Creates a Spring Security session on successful authentication.
     * <p>
     * Security:
     * - Requires CSRF token (sent in X-XSRF-TOKEN header)
     * - Creates HttpSession with Spring Security context
     * - Returns user details on success
     *
     * @param loginRequest JSON body with email and password
     * @param httpRequest HTTP request to create session
     * @param httpResponse HTTP response for remember-me cookie
     * @return User details after successful authentication
     */
    @PostMapping("/login")
    public ResponseEntity<UserDetailsDto> loginUser(
            @RequestBody LoginRequest loginRequest,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        UserDetailsDto userDetails = authService.login(loginRequest, httpRequest, httpResponse);
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
