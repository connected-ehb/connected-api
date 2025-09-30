package com.ehb.connected.domain.impl.users.services;

import com.ehb.connected.domain.impl.enrollments.entities.Enrollment;
import com.ehb.connected.domain.impl.enrollments.repositories.EnrollmentRepository;
import com.ehb.connected.domain.impl.tags.mappers.TagMapper;
import com.ehb.connected.domain.impl.users.dto.AuthUserDetailsDto;
import com.ehb.connected.domain.impl.users.dto.EmailRequestDto;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import com.ehb.connected.domain.impl.users.entities.Role;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.mappers.UserDetailsMapper;
import com.ehb.connected.domain.impl.users.repositories.UserRepository;
import com.ehb.connected.exceptions.AuthenticationRequiredException;
import com.ehb.connected.exceptions.BaseRuntimeException;
import com.ehb.connected.exceptions.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserDetailsMapper userDetailsMapper;
    private final TagMapper tagMapper;
    private final EnrollmentRepository enrollmentRepository;
    private final EmailService emailService;

    @Value("${connected.frontend-uri}")
    private String frontendUri;

    @Override
    public List<UserDetailsDto> getAllStudentsByCourseId(Long courseId) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        List<Long> canvasUserIds = enrollments.stream()
                .map(Enrollment::getCanvasUserId)
                .toList();
        List<User> users = userRepository.findByCanvasUserIdInAndRole(canvasUserIds, Role.STUDENT);
        return users.stream()
                .map(userDetailsMapper::toUserDetailsDto)
                .collect(Collectors.toList());
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(UserService.class, id));
    }

    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public UserDetailsDto updateUser(Principal principal, UserDetailsDto userDto) {
        User user = getUserByPrincipal(principal);

        user.setAboutMe(userDto.getAboutMe());
        user.setFieldOfStudy(userDto.getFieldOfStudy());
        user.setLinkedinUrl(userDto.getLinkedinUrl());
        user.setTags(new ArrayList<>(tagMapper.toEntityList(userDto.getTags())));
        return userDetailsMapper.toUserDetailsDto(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public User getUserByPrincipal(Principal principal) {
        if (principal == null) {
            throw new AuthenticationRequiredException();
        }

        String principalName = principal.getName();

        try {
            // Try to parse as Canvas ID (OAuth2 authentication)
            long canvasUserId = Long.parseLong(principalName);
            return userRepository.findByCanvasUserId(canvasUserId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found for canvas ID: " + canvasUserId));
        } catch (NumberFormatException e) {
            // Try as email (form-based authentication)
            return userRepository.findByEmail(principalName)
                    .orElseThrow(() -> new EntityNotFoundException("User not found for email: " + principalName));
        }
    }

    @Override
    public AuthUserDetailsDto getCurrentUser(OAuth2User principal) {
        if (principal == null) {
            return null;
        }

        User user = getUserFromOAuth2Principal(principal);
        return userDetailsMapper.toDtoWithPrincipal(user, principal);
    }

    @Override
    public void requestDeleteUser(Principal principal) {
        User user = getUserByPrincipal(principal);
        user.setDeleteRequestedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public void createEmailVerificationTokenByCanvasId(OAuth2User principal, EmailRequestDto emailRequestDto) {
        if (principal == null) {
            throw new AuthenticationRequiredException();
        }

        String canvasUserId = principal.getName();
        String email = emailRequestDto.getEmail();

        User user = userRepository.findByCanvasUserId(Long.parseLong(canvasUserId))
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!email.endsWith("@ehb.be") && !email.endsWith("@student.ehb.be")) {
            throw new BaseRuntimeException("Use a school email", HttpStatus.BAD_REQUEST);
        }

        String token = UUID.randomUUID().toString();
        user.setEmail(email);
        user.setEmailVerificationToken(token);
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusMinutes(15));
        user.setEmailVerified(false);
        userRepository.save(user);

        String url = frontendUri + "/verify?token=" + token;
        System.out.println(url);
        emailService.sendEmail(
                email,
                "Please verify your email",
                "verify-email",
                Map.of(
                        "url", url
                )
        );
    }

    @Override
    public void verifyEmailToken(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new BaseRuntimeException("Invalid or expired token", HttpStatus.BAD_REQUEST));

        if (user.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BaseRuntimeException("Token expired", HttpStatus.BAD_REQUEST);
        }

        String email = user.getEmail();
        if (email.endsWith("@student.ehb.be")) {
            user.setRole(Role.STUDENT);
        } else if (email.endsWith("@ehb.be")) {
            user.setRole(Role.TEACHER);
        } else {
            throw new BaseRuntimeException("Unsupported domain", HttpStatus.FORBIDDEN);
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        userRepository.save(user);
    }

    /**
     * Utility method to get a User from an OAuth2User principal
     */
    private User getUserFromOAuth2Principal(OAuth2User principal) {
        if (principal == null) {
            throw new AuthenticationRequiredException();
        }

        String principalName = principal.getName();

        try {
            // For OAuth2 users, the principal name is the canvasUserId
            long canvasUserId = Long.parseLong(principalName);
            return userRepository.findByCanvasUserId(canvasUserId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found for canvas ID: " + canvasUserId));
        } catch (NumberFormatException e) {
            // For form-based authentication, the principal name is the email
            return userRepository.findByEmail(principalName)
                    .orElseThrow(() -> new EntityNotFoundException("User not found for email: " + principalName));
        }
    }
}
