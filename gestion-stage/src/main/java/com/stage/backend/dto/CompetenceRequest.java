package com.stage.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CompetenceRequest {
    @NotBlank
    private String nom;
}