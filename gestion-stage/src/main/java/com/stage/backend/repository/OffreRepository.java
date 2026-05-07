package com.stage.backend.repository;

import com.stage.backend.entity.Offre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OffreRepository extends JpaRepository<Offre, Long> {

    List<Offre> findByRecruteurId(Long recruteurId);

    // Recherche pour TOUS les utilisateurs (étudiants) — sans filtre recruteur
    @Query("""
        SELECT DISTINCT o FROM Offre o
        LEFT JOIN o.competences c
        WHERE
            (:search IS NULL OR
             LOWER(o.titre) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR
             LOWER(o.description) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR
             LOWER(o.entreprise) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        AND (:secteur IS NULL OR LOWER(o.secteur) = LOWER(CAST(:secteur AS string)))
        AND (:competenceNom IS NULL OR LOWER(c.nom) LIKE LOWER(CONCAT('%', CAST(:competenceNom AS string), '%')))
    """)
    Page<Offre> searchOffres(
            @Param("search") String search,
            @Param("secteur") String secteur,
            @Param("competenceNom") String competenceNom,
            Pageable pageable
    );

    // Recherche filtrée par recruteur (pour le recruteur connecté)
    @Query("""
        SELECT DISTINCT o FROM Offre o
        LEFT JOIN o.competences c
        WHERE o.recruteur.id = :recruteurId
        AND (:search IS NULL OR
             LOWER(o.titre) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR
             LOWER(o.description) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR
             LOWER(o.entreprise) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        AND (:secteur IS NULL OR LOWER(o.secteur) = LOWER(CAST(:secteur AS string)))
        AND (:competenceNom IS NULL OR LOWER(c.nom) LIKE LOWER(CONCAT('%', CAST(:competenceNom AS string), '%')))
    """)
    Page<Offre> searchOffresByRecruteur(
            @Param("recruteurId") Long recruteurId,
            @Param("search") String search,
            @Param("secteur") String secteur,
            @Param("competenceNom") String competenceNom,
            Pageable pageable
    );
}