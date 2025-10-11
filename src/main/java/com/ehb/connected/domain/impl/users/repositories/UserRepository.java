package com.ehb.connected.domain.impl.users.repositories;

import com.ehb.connected.domain.impl.users.entities.Role;
import com.ehb.connected.domain.impl.users.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    List<User> findByCanvasUserIdInAndRole(List<Long> canvasUserIds, Role role);

    Optional<User> findByCanvasUserId(Long canvasUserId);

    List<User> findAllByRole(Role role);

    Optional<User> findByEmailVerificationToken(String token);

    @Query("""
              select u.id
              from User u
              where u.role = 'STUDENT' and u.canvasUserId in :canvasIds
            """)
    List<Long> findStudentIdsByCanvasUserIds(@Param("canvasIds") Collection<Long> canvasIds);
}
