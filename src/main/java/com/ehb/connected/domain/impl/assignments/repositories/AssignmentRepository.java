package com.ehb.connected.domain.impl.assignments.repositories;

import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByCourseId(Long courseId);
    Optional<Assignment> findByCanvasId(Long canvasId);
    boolean existsByCanvasId(Long canvasId);
}
