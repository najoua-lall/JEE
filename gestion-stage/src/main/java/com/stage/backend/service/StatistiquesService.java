package com.stage.backend.service;

import com.stage.backend.dto.StatistiquesResponse;
import com.stage.backend.entity.ERole;
import com.stage.backend.entity.StatutCandidature;
import com.stage.backend.repository.CandidatureRepository;
import com.stage.backend.repository.OffreRepository;
import com.stage.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatistiquesService {

    @Autowired private OffreRepository offreRepository;
    @Autowired private CandidatureRepository candidatureRepository;
    @Autowired private UserRepository userRepository;

    public StatistiquesResponse getStatistiques() {
        StatistiquesResponse stats = new StatistiquesResponse();

        // ── Totaux ──────────────────────────────────────────────────────────
        stats.setTotalOffres(offreRepository.count());
        stats.setTotalCandidatures(candidatureRepository.count());

        stats.setTotalEtudiants(
                userRepository.findAll().stream()
                        .filter(u -> u.getRoles().stream()
                                .anyMatch(r -> r.getName() == ERole.ROLE_ETUDIANT))
                        .count());

        stats.setTotalRecruteurs(
                userRepository.findAll().stream()
                        .filter(u -> u.getRoles().stream()
                                .anyMatch(r -> r.getName() == ERole.ROLE_RECRUTEUR))
                        .count());

        // ── Offres par secteur ──────────────────────────────────────────────
        Map<String, Long> parSecteur = offreRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        o -> (o.getSecteur() != null && !o.getSecteur().isBlank())
                                ? o.getSecteur() : "Non défini",
                        Collectors.counting()));
        stats.setOffreParSecteur(parSecteur);

        // ── Top compétences ─────────────────────────────────────────────────
        Map<String, Long> compCount = new HashMap<>();
        offreRepository.findAll().forEach(offre ->
                offre.getCompetences().forEach(c ->
                        compCount.merge(c.getNom(), 1L, Long::sum)));

        List<StatistiquesResponse.CompetenceStat> topCompetences = compCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(8)
                .map(e -> new StatistiquesResponse.CompetenceStat(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        stats.setTopCompetences(topCompetences);

        // ── Candidatures par statut ─────────────────────────────────────────
        Map<String, Long> parStatut = candidatureRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        c -> c.getStatut().name(),
                        Collectors.counting()));
        // S'assurer que les 3 statuts sont toujours présents
        for (StatutCandidature s : StatutCandidature.values()) {
            parStatut.putIfAbsent(s.name(), 0L);
        }
        stats.setCandidaturesParStatut(parStatut);

        return stats;
    }
}