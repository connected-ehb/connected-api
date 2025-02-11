package com.ehb.connected.domain.impl.applications.repositories;

import com.ehb.connected.domain.impl.applications.entities.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
}
