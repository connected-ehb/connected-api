package com.ehb.connected.domain.impl.courses.services;


import com.ehb.connected.domain.impl.courses.dto.CourseCreateDto;
import com.ehb.connected.domain.impl.courses.dto.CourseDetailsDto;
import com.ehb.connected.domain.impl.courses.entities.Course;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface CourseService {
    List<CourseDetailsDto> getNewCoursesFromCanvas(Authentication authentication);
    CourseDetailsDto createCourseWithEnrollments(Authentication authentication, CourseCreateDto courseDto);
    List<CourseDetailsDto> getCoursesByOwner(Authentication authentication);
    List<CourseDetailsDto> getCoursesByEnrollment(Authentication authentication);
    Course getCourseById(Long courseId);
    void deleteCourseById(Long courseId);
    CourseDetailsDto refreshEnrollments(Authentication authentication, Long courseId);
}
