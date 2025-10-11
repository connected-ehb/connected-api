package com.ehb.connected.domain.impl.users.services;

import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    private CustomUserDetailsService service;

    @BeforeEach
    void setUp() {
        service = new CustomUserDetailsService(userRepository);
    }

    @Test
    void loadUserByUsernameReturnsUser() {
        User user = new User();
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        UserDetails result = service.loadUserByUsername("user@example.com");

        assertThat(result).isSameAs(user);
    }

    @Test
    void loadUserByUsernameWhenMissingThrows() {
        when(userRepository.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("missing@example.com");
    }
}
