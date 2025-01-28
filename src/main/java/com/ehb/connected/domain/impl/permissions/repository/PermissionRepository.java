package com.ehb.connected.domain.impl.permissions.repository;

import com.ehb.connected.domain.impl.permissions.entities.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
}
