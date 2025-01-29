package com.ehb.connected.domain.impl.projects.repositories;

import com.ehb.connected.domain.impl.projects.entities.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
}
