package com.ehb.connected.domain.impl.enrollments.services;

import com.ehb.connected.domain.impl.courses.entities.Course;
import com.ehb.connected.domain.impl.enrollments.entities.Enrollment;
import com.ehb.connected.domain.impl.enrollments.repositories.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;

    @Override
    public void enrollUser(Course course, Long canvasUserId) {
        Enrollment enrollment = new Enrollment();
        enrollment.setCanvasUserId(canvasUserId);
        enrollment.setCourse(course);
        enrollmentRepository.save(enrollment);
    }
}
