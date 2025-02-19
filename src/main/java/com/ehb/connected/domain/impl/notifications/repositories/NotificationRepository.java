package com.ehb.connected.domain.impl.notifications.repositories;

import com.ehb.connected.domain.impl.notifications.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findById(Long id);

   List<Notification> findByUserId(Long userId);

    void deleteById(Long id);
}
