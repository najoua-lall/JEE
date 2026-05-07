package com.stage.backend.controller;

import com.stage.backend.dto.OffreRequest;
import com.stage.backend.dto.OffreResponse;
import com.stage.backend.service.OffreService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/offres")
@CrossOrigin(origins = "*")
public class OffreController {

    @Autowired
    private OffreService offreService;

    /** Toutes les offres (sans pagination) — conservé pour compatibilité */
    @GetMapping
    public ResponseEntity<List<OffreResponse>> getAllOffres() {
        return ResponseEntity.ok(offreService.getAllOffres());
    }

    /**
     * Recherche paginée pour les étudiants : toutes les offres de la plateforme.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<OffreResponse>> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String secteur,
            @RequestParam(required = false) String competence,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                offreService.searchOffres(search, secteur, competence, page, size));
    }

    /**
     * Recherche paginée pour un recruteur : uniquement SES offres.
     */
    @GetMapping("/mes-offres")
    public ResponseEntity<Page<OffreResponse>> mesOffres(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String secteur,
            @RequestParam(required = false) String competence,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                offreService.searchOffresParRecruteur(
                        userDetails.getUsername(),
                        search, secteur, competence, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OffreResponse> getOffre(@PathVariable Long id) {
        return ResponseEntity.ok(offreService.getOffreById(id));
    }

    @PostMapping
    public ResponseEntity<OffreResponse> createOffre(
            @Valid @RequestBody OffreRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(offreService.createOffre(request, userDetails.getUsername()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OffreResponse> updateOffre(
            @PathVariable Long id,
            @Valid @RequestBody OffreRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                offreService.updateOffre(id, request, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffre(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        offreService.deleteOffre(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/competences")
    public ResponseEntity<OffreResponse> associerCompetences(
            @PathVariable Long id,
            @RequestBody Set<Long> competenceIds,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                offreService.associerCompetences(id, competenceIds, userDetails.getUsername()));
    }
}