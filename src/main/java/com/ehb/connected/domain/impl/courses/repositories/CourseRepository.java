package com.ehb.connected.domain.impl.courses.repositories;

import com.ehb.connected.domain.impl.courses.entities.Course;
import com.ehb.connected.domain.impl.users.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByOwner(User owner);
}
