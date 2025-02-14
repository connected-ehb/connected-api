package com.ehb.connected.domain.impl.users.service;

import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.repositories.UserRepository;
import com.ehb.connected.domain.impl.users.services.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userServiceImpl;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllUsers() {
        User user1 = new User();
        User user2 = new User();
        List<User> users = Arrays.asList(user1, user2);

        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userServiceImpl.getAllUsers();
        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    public void testGetUserById() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("User 1");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userServiceImpl.getUserById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("User 1", result.getFirstName());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    public void testCreateUser() {
        User user = new User();
        user.setFirstName("New User");

        when(userRepository.save(user)).thenReturn(user);

        User result = userServiceImpl.createUser(user);
        assertNotNull(result);
        assertEquals("New User", result.getFirstName());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testUpdateUser() {
        //TODO
    }

    @Test
    public void testDeleteUser() {
        Long userId = 1L;

        // Act
        userServiceImpl.deleteUser(userId);

        // Assert
        verify(userRepository, times(1)).deleteById(userId);
    }
}