package com.ehb.connected.domain.impl.bugs.repositories;

import com.ehb.connected.domain.impl.bugs.entities.Bug;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BugRepository extends JpaRepository<Bug,Long> {
}
