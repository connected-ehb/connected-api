package com.ehb.connected.domain.impl.courses.repositories;

import com.ehb.connected.domain.impl.courses.entities.Course;
import com.ehb.connected.domain.impl.users.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByOwner(User owner);
    List<Course> findByEnrollmentsCanvasUserId(Long canvasUserId);

    Optional<Course> findByCanvasId(Long canvasId);
    boolean existsByCanvasId(Long canvasId);
}
