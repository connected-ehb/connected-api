package com.ehb.connected.domain.impl.courses.services;

import com.ehb.connected.domain.impl.courses.entities.Course;
import com.ehb.connected.domain.impl.courses.repositories.CourseRepository;
import com.ehb.connected.domain.impl.users.services.UserServiceImpl;
import jakarta.persistence.EntityNotFoundException;
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
    public List<Course> getCoursesByOwner(Principal principal) {
        return courseRepository.findByOwner(userService.getUserByEmail(principal.getName()));
    }

    @Override
    public List<Course> getCoursesByEnrollment(Principal principal) {
        return courseRepository.findByEnrollmentsCanvasUserId(userService.getUserByEmail(principal.getName()).getCanvasUserId());
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

    @Override
    public Course getCourseById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course does not exist"));
    }

    @Override
    public Course getCourseByCanvasCourseId(Long canvasCourseId) {
        return courseRepository.findByCanvasCourseId(canvasCourseId)
                .orElseThrow(() -> new EntityNotFoundException("Course does not exist"));
    }
}
