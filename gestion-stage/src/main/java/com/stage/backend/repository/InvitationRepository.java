package com.stage.backend.repository;

import com.stage.backend.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByCandidatureId(Long candidatureId);
}