package com.ehb.connected.domain.impl.users.services;

import com.ehb.connected.domain.impl.enrollments.entities.Enrollment;
import com.ehb.connected.domain.impl.enrollments.repositories.EnrollmentRepository;
import com.ehb.connected.domain.impl.tags.mappers.TagMapper;
import com.ehb.connected.domain.impl.users.dto.EmailRequestDto;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import com.ehb.connected.domain.impl.users.entities.Role;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.mappers.UserDetailsMapper;
import com.ehb.connected.domain.impl.users.repositories.UserRepository;
import com.ehb.connected.exceptions.BaseRuntimeException;
import com.ehb.connected.exceptions.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public UserDetailsDto updateUser(Principal principal, UserDetailsDto userDto) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow(() -> new RuntimeException("User not found"));

        if (!Objects.equals(user.getEmail(), principal.getName())) {
            throw new RuntimeException("User is not owner of the profile");
        }

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
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found for email: " + principal.getName()));
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public List<User> getAllUsersByRole(Role role) {
        return userRepository.findAllByRole(role);
    }

    @Override
    public void requestDeleteUser(Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        user.setDeleteRequestedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public void createEmailVerificationToken(User principal, EmailRequestDto emailRequestDto) {
        Long canvasUserId = principal.getCanvasUserId();
        String email = emailRequestDto.getEmail();
        User user = userRepository.findByCanvasUserId(canvasUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!email.endsWith("@ehb.be") && !email.endsWith("@student.ehb.be")) {
            throw new BaseRuntimeException("Use a school email", HttpStatus.BAD_REQUEST);
        }

        String token = UUID.randomUUID().toString();
        user.setEmail(email);
        user.setEmailVerificationToken(token);
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusMinutes(15));
        user.setEmailVerified(false);
        userRepository.save(user);

        String baseUrl = "http://localhost:8080";

        String url = baseUrl + "/verify?token=" + token;
        System.out.println(url);
        emailService.sendVerificationEmail(email, url);
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
}
