package com.ehb.connected.domain.impl.courses.services;


import com.ehb.connected.domain.impl.courses.dto.CourseCreateDto;
import com.ehb.connected.domain.impl.courses.dto.CourseDetailsDto;
import com.ehb.connected.domain.impl.courses.entities.Course;

import java.security.Principal;
import java.util.List;

public interface CourseService {
    List<CourseDetailsDto> getNewCoursesFromCanvas(Principal principal);
    CourseDetailsDto createCourseWithEnrollments(Principal principal, CourseCreateDto courseDto);
    List<CourseDetailsDto> getCoursesByOwner(Principal principal);
    List<CourseDetailsDto> getCoursesByEnrollment(Principal principal);
    Course getCourseById(Long courseId);

    Course getCourseByCanvasCourseId(Long canvasCourseId);
}
