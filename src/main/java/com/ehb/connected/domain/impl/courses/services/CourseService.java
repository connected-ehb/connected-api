package com.ehb.connected.domain.impl.courses.services;


import com.ehb.connected.domain.impl.courses.entities.Course;

import java.security.Principal;
import java.util.List;

public interface CourseService {
    List<Course> getCoursesByOwner(Principal principal);
    List<Course> getCoursesByEnrollment(Principal principal);
    void createCourse(Course course);

    Course getCourseById(Long courseId);
}
