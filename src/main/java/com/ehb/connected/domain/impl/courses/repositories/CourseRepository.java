package com.ehb.connected.domain.impl.courses.repositories;

import com.ehb.connected.domain.impl.courses.entities.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
