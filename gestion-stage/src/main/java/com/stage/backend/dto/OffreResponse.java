package com.stage.backend.dto;

import com.stage.backend.entity.Offre;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Getter @Setter
public class OffreResponse {
    private Long id;
    private String titre;
    private String description;
    private String entreprise;
    private String localisation;
    private String secteur;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String recruteurUsername;
    private Set<CompetenceResponse> competences;

    public OffreResponse(Offre offre) {
        this.id = offre.getId();
        this.titre = offre.getTitre();
        this.description = offre.getDescription();
        this.entreprise = offre.getEntreprise();
        this.localisation = offre.getLocalisation();
        this.secteur = offre.getSecteur();
        this.dateDebut = offre.getDateDebut();
        this.dateFin = offre.getDateFin();
        this.recruteurUsername = offre.getRecruteur().getUsername();
        this.competences = offre.getCompetences()
                .stream()
                .map(CompetenceResponse::new)
                .collect(Collectors.toSet());
    }
}