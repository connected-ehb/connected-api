package com.ehb.connected.domain.impl.users.controllers;

import com.ehb.connected.domain.impl.users.dto.EmailRequestDto;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.mappers.UserDetailsMapper;
import com.ehb.connected.domain.impl.users.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserDetailsMapper userDetailsMapper;

    @Test
    void getUserById_ReturnsUserDetailsDtoAsJson() throws Exception {
        User user = new User();
        user.setId(5L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@doe.com");

        UserDetailsDto dto = new UserDetailsDto();
        dto.setId(5L);
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john@doe.com");

        when(userService.getUserById(5L)).thenReturn(user);
        when(userDetailsMapper.toUserDetailsDto(user)).thenReturn(dto);

        mockMvc.perform(get("/api/users/5").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john@doe.com"));

        verify(userService).getUserById(5L);
        verify(userDetailsMapper).toUserDetailsDto(user);
    }

    @Test
    void createUser_ReturnsCreatedUserAsJson() throws Exception {
        User payload = new User();
        payload.setEmail("new@user.com");
        payload.setFirstName("New");
        payload.setLastName("User");

        User created = new User();
        created.setId(10L);
        created.setEmail("new@user.com");
        created.setFirstName("New");
        created.setLastName("User");

        when(userService.createUser(any(User.class))).thenReturn(created);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.email").value("new@user.com"))
                .andExpect(jsonPath("$.firstName").value("New"))
                .andExpect(jsonPath("$.lastName").value("User"));

        verify(userService).createUser(any(User.class));
    }

    @Test
    void updateUser_ReturnsUpdatedUserDto() throws Exception {
        Principal principal = () -> "mockUser";
        UserDetailsDto request = new UserDetailsDto();
        request.setEmail("updated@user.com");

        UserDetailsDto updated = new UserDetailsDto();
        updated.setEmail("updated@user.com");
        updated.setFirstName("Updated");
        updated.setLastName("User");

        when(userService.updateUser(eq(principal), any(UserDetailsDto.class))).thenReturn(updated);

        mockMvc.perform(patch("/api/users/update")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("updated@user.com"))
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("User"));

        verify(userService).updateUser(eq(principal), any(UserDetailsDto.class));
    }

    @Test
    void requestDeleteUser_ReturnsOkStatus() throws Exception {
        Principal principal = () -> "mockUser";

        mockMvc.perform(post("/api/users/request-delete").principal(principal))
                .andExpect(status().isOk());

        verify(userService).requestDeleteUser(principal);
    }

    @Test
    void sendVerificationEmail_ReturnsOkStatus() throws Exception {
        EmailRequestDto dto = new EmailRequestDto();

        mockMvc.perform(post("/api/users/send-verification-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(userService).createEmailVerificationTokenByCanvasId(any(), any(EmailRequestDto.class));
    }

    @Test
    void verifyToken_ReturnsOkStatus() throws Exception {
        mockMvc.perform(get("/api/users/verify").param("token", "abc123"))
                .andExpect(status().isOk());

        verify(userService).verifyEmailToken("abc123");
    }
}
