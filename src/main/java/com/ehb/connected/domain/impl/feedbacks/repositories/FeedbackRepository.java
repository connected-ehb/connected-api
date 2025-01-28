package com.ehb.connected.domain.impl.feedbacks.repositories;

import com.ehb.connected.domain.impl.feedbacks.entities.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}
