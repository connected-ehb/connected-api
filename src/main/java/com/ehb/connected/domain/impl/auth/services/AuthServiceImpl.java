package com.ehb.connected.domain.impl.auth.services;

import com.ehb.connected.domain.impl.auth.entities.LoginRequestDto;
import com.ehb.connected.domain.impl.auth.entities.RegistrationRequestDto;
import com.ehb.connected.domain.impl.canvas.CanvasAuthService;
import com.ehb.connected.domain.impl.invitations.services.InvitationService;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import com.ehb.connected.domain.impl.users.entities.Role;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.mappers.UserDetailsMapper;
import com.ehb.connected.domain.impl.users.repositories.UserRepository;
import com.ehb.connected.exceptions.BaseRuntimeException;
import com.ehb.connected.exceptions.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserDetailsMapper userDetailsMapper;
    private final CanvasAuthService canvasAuthService;
    private final InvitationService invitationService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetailsDto register(RegistrationRequestDto request) {
        // Validate the invitation code (expires after one use or 24 hours)
        if (!invitationService.validateInvitationCode(request.getInvitationCode())) {
            throw new BaseRuntimeException("Invalid or expired invitation code.", HttpStatus.BAD_REQUEST);
        }

        // Check if a user with the given email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BaseRuntimeException("A user with this email already exists.", HttpStatus.CONFLICT);
        }

        // Create and configure a new User entity
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        // Encode the password before saving!
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.RESEARCHER);
        // Save the new user
        user = userRepository.save(user);

        // Mark the invitation code as used
        invitationService.markInvitationAsUsed(request.getInvitationCode());

        return userDetailsMapper.toUserDetailsDto(user);
    }

    @Override
    public UserDetailsDto login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BaseRuntimeException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }
        return userDetailsMapper.toUserDetailsDto(user);
    }

    @Override
    public void logout(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (user.getAccessToken() != null) {
            canvasAuthService.deleteAccessToken(user.getAccessToken());
        }
        user.setAccessToken(null);
        user.setRefreshToken(null);
        userRepository.save(user);
    }
}
