package com.stage.backend.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AcceptationRequest {
    private Long candidatureId;
    private String societeAccueil;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String mode;            // PRESENTIEL, DISTANCE, HYBRIDE
    private boolean remunere;
    private String descriptionSujet;
    private String conditionsComplementaires;
}