package com.ehb.connected.domain.impl.deadlines.repositories;

import com.ehb.connected.domain.impl.deadlines.entities.Deadline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeadlineRepository extends JpaRepository<Deadline, Long> {
}
