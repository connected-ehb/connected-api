package com.ehb.connected.domain.impl.users.services;


import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import com.ehb.connected.domain.impl.users.entities.User;

import java.security.Principal;
import java.util.List;

public interface UserService {

    List<User> getAllUsers();
    User getUserById(Long id);
    User createUser(User user);
    UserDetailsDto updateUser(Principal principal, UserDetailsDto user);
    void deleteUser(Long id);
    User getUserByEmail(String email);

    User getUserByCanvasUserId(Long canvasUserId);
}
