package com.stage.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.Set;

@Getter @Setter
public class OffreRequest {
    @NotBlank
    private String titre;

    @NotBlank
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

    private Set<Long> competenceIds;
}