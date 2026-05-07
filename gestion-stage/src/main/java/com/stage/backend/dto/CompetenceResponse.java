package com.stage.backend.dto;

import com.stage.backend.entity.Competence;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CompetenceResponse {
    private Long id;
    private String nom;

    public CompetenceResponse(Competence c) {
        this.id = c.getId();
        this.nom = c.getNom();
    }
}