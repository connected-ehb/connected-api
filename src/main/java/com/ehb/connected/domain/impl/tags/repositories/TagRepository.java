package com.ehb.connected.domain.impl.tags.repositories;

import com.ehb.connected.domain.impl.tags.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByNameContainingIgnoreCase(String query);
}
