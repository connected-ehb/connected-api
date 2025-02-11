package com.ehb.connected.domain.impl.courses.services;


import com.ehb.connected.domain.impl.courses.entities.Course;

import java.security.Principal;
import java.util.List;

public interface CourseService {
    List<Course> getCourses(Principal principal);
    void createCourse(Course course);
}
