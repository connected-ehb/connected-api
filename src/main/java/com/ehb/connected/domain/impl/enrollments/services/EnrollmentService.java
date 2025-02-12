package com.ehb.connected.domain.impl.enrollments.services;

import com.ehb.connected.domain.impl.courses.entities.Course;

public interface EnrollmentService {
    void enrollUser(Course course, Long canvasUserId);
}
