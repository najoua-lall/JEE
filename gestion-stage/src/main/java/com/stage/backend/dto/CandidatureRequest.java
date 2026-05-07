package com.stage.backend.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter @Setter
public class CandidatureRequest {
    private String lettreMotivation;
    private LocalDate disponibilite;
    private String telephone;
    private String niveauEtudes;
    private String etablissement;
    private String linkedinUrl;
    private String portfolioUrl;
}