package com.ehb.connected.domain.impl.assignments.repositories;

import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
}
