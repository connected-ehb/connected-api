package com.ehb.connected.domain.impl.roles.repositories;

import com.ehb.connected.domain.impl.roles.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
