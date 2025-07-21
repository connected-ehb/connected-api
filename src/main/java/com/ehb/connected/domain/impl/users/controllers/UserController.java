package com.ehb.connected.domain.impl.users.controllers;


import com.ehb.connected.domain.impl.users.dto.EmailRequestDto;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.mappers.UserDetailsMapper;
import com.ehb.connected.domain.impl.users.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserDetailsMapper userDetailsMapper;

    @GetMapping("/{id}")
    public UserDetailsDto getUserById(@PathVariable Long id){
        User user = userService.getUserById(id);
        return userDetailsMapper.toUserDetailsDto(user);
    }

    @PostMapping()
    public User createUser(User user){
        return userService.createUser(user);
    }

    //only the owner of the user can update the user
    @PatchMapping("/update")
    public ResponseEntity<UserDetailsDto> updateUser(Principal principal, @RequestBody UserDetailsDto userDetailsDto){
        return ResponseEntity.ok(userService.updateUser(principal, userDetailsDto));
    }

    @PostMapping("/request-delete")
    public ResponseEntity<Map<String, String>> requestDeleteUser(Principal principal){
        userService.requestDeleteUser(principal);
        Map<String, String> response = new HashMap<>();
        response.put("message", "User deletion requested and will be processed in 60 days");
        return ResponseEntity.ok(response);
    }

    @PostMapping("send-verification-email")
    public ResponseEntity<Void> sendVerificationEmail(@AuthenticationPrincipal User principal, @RequestBody EmailRequestDto emailRequestDto){
        userService.createEmailVerificationToken(principal, emailRequestDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/verify")
    public ResponseEntity<Void> verifyToken(@RequestParam String token) {
        userService.verifyEmailToken(token);
        return ResponseEntity.ok().build();
    }
}
