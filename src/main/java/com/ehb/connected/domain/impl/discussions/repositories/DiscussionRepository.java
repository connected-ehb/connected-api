package com.ehb.connected.domain.impl.discussions.repositories;

import com.ehb.connected.domain.impl.discussions.entities.Discussion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscussionRepository extends JpaRepository<Discussion, Long> {
}
