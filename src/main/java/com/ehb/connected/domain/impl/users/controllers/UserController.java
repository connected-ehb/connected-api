package com.ehb.connected.domain.impl.users.controllers;

import com.ehb.connected.domain.impl.users.dto.EmailRequestDto;
import com.ehb.connected.domain.impl.users.dto.MinimalUserDetailsDto;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.mappers.UserDetailsMapper;
import com.ehb.connected.domain.impl.users.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserDetailsMapper userDetailsMapper;

    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping("/{id}")
    public ResponseEntity<MinimalUserDetailsDto> getUserById(@PathVariable Long id){
        User user = userService.getUserById(id);
        return ResponseEntity.ok(userDetailsMapper.toMinimalUserDetailsDto(user));
    }

    @PreAuthorize("hasAuthority('user:create')")
    @PostMapping()
    public ResponseEntity<User> createUser(@RequestBody User user){
        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(createdUser);
    }

    @PreAuthorize("hasAuthority('user:me:update')")
    @PatchMapping("/me")
    public ResponseEntity<UserDetailsDto> updateCurrentUser(Authentication authentication, @RequestBody UserDetailsDto userDetailsDto){
        UserDetailsDto updatedUser = userService.updateUser(authentication, userDetailsDto);
        return ResponseEntity.ok(updatedUser);
    }

    @PreAuthorize("hasAuthority('user:update')")
    @PatchMapping("/{userId}")
    public ResponseEntity<UserDetailsDto> updateUserAsAdmin(@PathVariable Long userId, @RequestBody UserDetailsDto userDetailsDto) {
        UserDetailsDto updatedUser = userService.updateUserAsAdmin(userId, userDetailsDto);
        return ResponseEntity.ok(updatedUser);
    }

    @PreAuthorize("hasAuthority('user:me:request_delete')")
    @PostMapping("/me/request-delete")
    public ResponseEntity<Void> requestDeleteUser(Authentication authentication){
        userService.requestDeleteUser(authentication);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send-verification-email")
    public ResponseEntity<Void> sendVerificationEmail(@AuthenticationPrincipal OAuth2User principal, @RequestBody EmailRequestDto emailRequestDto){
        userService.createEmailVerificationTokenByCanvasId(principal, emailRequestDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/verify")
    public ResponseEntity<Void> verifyToken(@RequestParam String token) {
        userService.verifyEmailToken(token);
        return ResponseEntity.ok().build();
    }
}
