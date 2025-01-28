package com.ehb.connected.domain.impl.deadlines.repositories;

import com.ehb.connected.domain.impl.deadlines.entities.Deadline;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeadlineRepository extends JpaRepository<Deadline, Long> {
}
