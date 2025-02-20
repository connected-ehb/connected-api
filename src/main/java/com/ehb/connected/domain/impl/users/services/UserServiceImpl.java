package com.ehb.connected.domain.impl.users.services;


import com.ehb.connected.domain.impl.enrollments.entities.Enrollment;
import com.ehb.connected.domain.impl.enrollments.repositories.EnrollmentRepository;
import com.ehb.connected.domain.impl.tags.mappers.TagMapper;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.mappers.UserDetailsMapper;
import com.ehb.connected.domain.impl.users.repositories.UserRepository;
import com.ehb.connected.exceptions.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final UserDetailsMapper userDetailsMapper;
    private final TagMapper tagMapper;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    public List<UserDetailsDto> getAllUsersByCourseId(Long courseId) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        List<Long> canvasUserIds = enrollments.stream()
                .map(Enrollment::getCanvasUserId)
                .toList();
        List<User> users = userRepository.findByCanvasUserIdIn(canvasUserIds);
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
}
