package com.ehb.connected.domain.impl.enrollments.repositories;

import com.ehb.connected.domain.impl.enrollments.entities.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
}
