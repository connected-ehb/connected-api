package com.ehb.connected.domain.impl.courses.repositories;

import com.ehb.connected.domain.impl.courses.entities.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
}
