package com.ehb.connected.domain.impl.notifications.repositories;

import com.ehb.connected.domain.impl.notifications.entities.Notification;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface NotificationRepository {

    void save(Notification notification);
    
    Notification findById(Long id);

   List<Notification> findByUserId(Long userId);

    void deleteById(Long id);
}
