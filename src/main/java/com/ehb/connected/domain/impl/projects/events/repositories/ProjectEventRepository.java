package com.ehb.connected.domain.impl.projects.events.repositories;

import com.ehb.connected.domain.impl.projects.events.entities.ProjectEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectEventRepository extends JpaRepository<ProjectEvent, Long> {
    List<ProjectEvent> findAllByProjectIdOrderByTimestampDesc(Long projectId);
}
