package com.stage.backend.dto;

import com.stage.backend.entity.Candidature;
import com.stage.backend.entity.StatutCandidature;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
public class CandidatureResponse {
    private Long id;
    private Long offreId;
    private String offreTitre;
    private String offreEntreprise;
    private String etudiantUsername;
    private String etudiantEmail;
    private StatutCandidature statut;
    private LocalDateTime datePostulation;

    // Infos candidature
    private String lettreMotivation;
    private String cvFileName;
    private LocalDate disponibilite;
    private String telephone;
    private String niveauEtudes;
    private String etablissement;
    private String linkedinUrl;
    private String portfolioUrl;

    public CandidatureResponse(Candidature c) {
        this.id               = c.getId();
        this.offreId          = c.getOffre().getId();
        this.offreTitre       = c.getOffre().getTitre();
        this.offreEntreprise  = c.getOffre().getEntreprise();
        this.etudiantUsername = c.getEtudiant().getUsername();
        this.etudiantEmail    = c.getEtudiant().getEmail();
        this.statut           = c.getStatut();
        this.datePostulation  = c.getDatePostulation();
        this.lettreMotivation = c.getLettreMotivation();
        this.cvFileName       = c.getCvFileName();
        this.disponibilite    = c.getDisponibilite();
        this.telephone        = c.getTelephone();
        this.niveauEtudes     = c.getNiveauEtudes();
        this.etablissement    = c.getEtablissement();
        this.linkedinUrl      = c.getLinkedinUrl();
        this.portfolioUrl     = c.getPortfolioUrl();
    }
}