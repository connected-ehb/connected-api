package com.ehb.connected.domain.impl.users.services;


import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import com.ehb.connected.domain.impl.users.entities.Role;
import com.ehb.connected.domain.impl.users.entities.User;

import java.security.Principal;
import java.util.List;

public interface UserService {

    List<UserDetailsDto> getAllStudentsByCourseId(Long courseId);
    User getUserById(Long id);
    User createUser(User user);
    UserDetailsDto updateUser(Principal principal, UserDetailsDto user);
    void deleteUser(Long id);
    User getUserByPrincipal(Principal principal);
    User getUserByEmail(String email);

    List<User> getAllUsersByRole(Role role);

    void logout(Principal principal);
}
