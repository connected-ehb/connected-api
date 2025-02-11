package com.ehb.connected.domain.impl.courses.services;

import com.ehb.connected.domain.impl.courses.entities.Course;
import com.ehb.connected.domain.impl.courses.repositories.CourseRepository;
import com.ehb.connected.domain.impl.users.services.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;
    private final UserServiceImpl userService;

    @Override
    public List<Course> getCourses(Principal principal) {
        return courseRepository.findByOwner(userService.getUserByEmail(principal.getName()));
    }

    @Override
    public void createCourse(Course course) {
        try {
            courseRepository.save(course);
        } catch (Exception e) {
            System.out.println("Error while creating course: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
