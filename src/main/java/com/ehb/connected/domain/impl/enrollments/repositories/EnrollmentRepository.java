package com.ehb.connected.domain.impl.enrollments.repositories;

import com.ehb.connected.domain.impl.enrollments.entities.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByCourseId(Long courseId);

    @Modifying
    @Query("DELETE FROM Enrollment e WHERE e.course.id = :courseId")
    void deleteByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId")
    long countByCourseId(@Param("courseId") Long courseId);

    @Query("""
              select e.canvasUserId
              from Enrollment e
              where e.course.id = :courseId
            """)
    List<Long> findCanvasUserIdsByCourse(@Param("courseId") Long courseId);
}
