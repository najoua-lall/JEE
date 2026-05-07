package com.stage.backend.dto;

import com.stage.backend.entity.StatutCandidature;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class StatutRequest {
    private StatutCandidature statut;
}