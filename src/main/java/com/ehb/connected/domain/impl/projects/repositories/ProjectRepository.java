package com.ehb.connected.domain.impl.projects.repositories;

import com.ehb.connected.domain.impl.projects.entities.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByAssignmentId(Long assignmentId);
}
