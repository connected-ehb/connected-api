package com.ehb.connected.domain.impl.feedbacks.repositories;

import com.ehb.connected.domain.impl.feedbacks.entities.Feedback;
import com.ehb.connected.domain.impl.projects.entities.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findAllByProject(Project project);
}
