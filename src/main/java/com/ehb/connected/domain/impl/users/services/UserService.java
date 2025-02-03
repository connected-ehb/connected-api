package com.ehb.connected.domain.impl.users.services;


import com.ehb.connected.domain.impl.users.entities.User;

import java.util.List;

public interface UserService {

    List<User> getAllUsers();
    User getUserById(Long id);
    User createUser(User user);
    User updateUser(User user);
    void deleteUser(Long id);
    User getUserByEmail(String email);
}
