package com.ehb.connected.domain.impl.enrollments.services;

import com.ehb.connected.domain.impl.courses.entities.Course;

import java.util.List;

public interface EnrollmentService {
    void enrollUser(Course course, Long canvasUserId);
    void replaceCourseEnrollments(Course course, List<Long> canvasUserIds);
    long countByCourseId(Long courseId);
}
