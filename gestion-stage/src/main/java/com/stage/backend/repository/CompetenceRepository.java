package com.stage.backend.repository;

import com.stage.backend.entity.Competence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CompetenceRepository extends JpaRepository<Competence, Long> {
    Optional<Competence> findByNom(String nom);
    boolean existsByNom(String nom);
}