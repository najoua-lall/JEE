package com.stage.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "invitations")
@Getter @Setter @NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "candidature_id", nullable = false, unique = true)
    private Candidature candidature;

    @Column(nullable = false)
    private String societeAccueil;

    @Column(nullable = false)
    private LocalDate dateDebut;

    @Column(nullable = false)
    private LocalDate dateFin;

    @Column(nullable = false)
    private String mode; // PRESENTIEL, DISTANCE, HYBRIDE

    @Column(nullable = false)
    private boolean remunere;

    @Column(length = 2000)
    private String descriptionSujet;

    @Column(length = 1000)
    private String conditionsComplementaires;
}