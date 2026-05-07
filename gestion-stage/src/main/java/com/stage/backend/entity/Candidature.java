package com.stage.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "candidatures",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"etudiant_id", "offre_id"}
        ))
@Getter @Setter @NoArgsConstructor
public class Candidature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private User etudiant;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "offre_id", nullable = false)
    private Offre offre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCandidature statut = StatutCandidature.EN_ATTENTE;

    @Column(nullable = false)
    private LocalDateTime datePostulation = LocalDateTime.now();

    // ── Informations candidature ──────────────────────

    @Column(length = 3000, nullable = false)
    private String lettreMotivation;

    @Column(nullable = false)
    private String cvFileName;        // nom original du fichier

    @Column(nullable = false)
    private String cvFilePath;        // chemin sur le serveur

    @Column(nullable = false)
    private LocalDate disponibilite;

    @Column(nullable = false)
    private String telephone;

    @Column(nullable = false)
    private String niveauEtudes;      // Bac+3, Bac+4, Bac+5

    @Column(nullable = false)
    private String etablissement;

    private String linkedinUrl;
    private String portfolioUrl;
}