package com.ehb.connected.domain.impl.users.controllers;


import com.ehb.connected.domain.impl.tags.mappers.TagMapper;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.mappers.UserDetailsMapper;
import com.ehb.connected.domain.impl.users.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserDetailsMapper userDetailsMapper;

    @GetMapping
    public List<User> getAllUsers(){
        return userService.getAllUsers();
    }

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
}
