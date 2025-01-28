package com.ehb.connected.domain.impl.applications.repositories;

import com.ehb.connected.domain.impl.applications.entities.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
}
