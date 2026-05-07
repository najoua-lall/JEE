package com.stage.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "offres")
@Getter @Setter @NoArgsConstructor
public class Offre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String titre;

    @NotBlank
    @Column(length = 2000)
    private String description;

    @NotBlank
    private String entreprise;

    @NotBlank
    private String localisation;

    private String secteur;

    @NotNull
    private LocalDate dateDebut;

    @NotNull
    private LocalDate dateFin;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "recruteur_id", nullable = false)
    private User recruteur;

    @ManyToMany
    @JoinTable(
            name = "offre_competences",
            joinColumns = @JoinColumn(name = "offre_id"),
            inverseJoinColumns = @JoinColumn(name = "competence_id")
    )
    private Set<Competence> competences = new HashSet<>();
}