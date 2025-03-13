package com.ehb.connected.domain.impl.invitations.repositories;

import com.ehb.connected.domain.impl.invitations.entities.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    @Query("SELECT i FROM Invitation i WHERE i.code = ?1")
    Optional<Invitation> findByCode(String code);
    void deleteByCode(String code);
}
