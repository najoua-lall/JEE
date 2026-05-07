package com.stage.backend.repository;

import com.stage.backend.entity.Candidature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CandidatureRepository extends JpaRepository<Candidature, Long> {

    // Mes candidatures (étudiant)
    List<Candidature> findByEtudiantId(Long etudiantId);

    // Candidatures reçues pour une offre (recruteur)
    List<Candidature> findByOffreId(Long offreId);

    // Vérifier si déjà postulé
    boolean existsByEtudiantIdAndOffreId(Long etudiantId, Long offreId);

    // Trouver une candidature spécifique
    Optional<Candidature> findByEtudiantIdAndOffreId(Long etudiantId, Long offreId);
}