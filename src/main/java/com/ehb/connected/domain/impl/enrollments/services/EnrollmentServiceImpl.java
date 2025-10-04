package com.ehb.connected.domain.impl.enrollments.services;

import com.ehb.connected.domain.impl.courses.entities.Course;
import com.ehb.connected.domain.impl.enrollments.entities.Enrollment;
import com.ehb.connected.domain.impl.enrollments.repositories.EnrollmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Transactional
    @Override
    public void replaceCourseEnrollments(Course course, List<Long> canvasUserIds) {
        enrollmentRepository.deleteByCourseId(course.getId());

        Set<Long> unique = new HashSet<>(canvasUserIds);
        for (Long id : unique) {
            enrollUser(course, id);
        }
    }

    @Override
    public long countByCourseId(Long courseId) {
        return enrollmentRepository.countByCourseId(courseId);
    }
}
