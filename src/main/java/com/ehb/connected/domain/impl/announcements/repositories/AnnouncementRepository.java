package com.ehb.connected.domain.impl.announcements.repositories;

import com.ehb.connected.domain.impl.announcements.entities.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findAllByAssignmentIdOrderByCreatedAtDesc(Long assignmentId);
}
