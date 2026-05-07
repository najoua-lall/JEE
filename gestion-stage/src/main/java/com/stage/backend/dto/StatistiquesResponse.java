package com.stage.backend.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class StatistiquesResponse {

    private long totalOffres;
    private long totalCandidatures;
    private long totalEtudiants;
    private long totalRecruteurs;

    // { "secteur": count }
    private Map<String, Long> offreParSecteur;

    // Top N compétences : [ {nom, count} ]
    private List<CompetenceStat> topCompetences;

    // { "EN_ATTENTE": x, "ACCEPTEE": y, "REFUSEE": z }
    private Map<String, Long> candidaturesParStatut;

    @Data
    public static class CompetenceStat {
        private String nom;
        private long count;

        public CompetenceStat(String nom, long count) {
            this.nom   = nom;
            this.count = count;
        }
    }
}