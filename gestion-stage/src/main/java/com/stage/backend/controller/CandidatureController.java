package com.stage.backend.controller;

import com.stage.backend.dto.AcceptationRequest;
import com.stage.backend.dto.CandidatureRequest;
import com.stage.backend.dto.CandidatureResponse;
import com.stage.backend.dto.StatutRequest;
import com.stage.backend.service.CandidatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/candidatures")
@CrossOrigin(origins = "*")
public class CandidatureController {

    @Autowired
    private CandidatureService candidatureService;

    // ─── POSTULER (existant) ─────────────────────────────────────────────
    @PostMapping(value = "/postuler/{offreId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CandidatureResponse> postuler(
            @PathVariable Long offreId,
            @RequestParam("lettreMotivation") String lettreMotivation,
            @RequestParam("disponibilite") String disponibilite,
            @RequestParam("telephone") String telephone,
            @RequestParam("niveauEtudes") String niveauEtudes,
            @RequestParam("etablissement") String etablissement,
            @RequestParam(value = "linkedinUrl", required = false) String linkedinUrl,
            @RequestParam(value = "portfolioUrl", required = false) String portfolioUrl,
            @RequestParam("cv") MultipartFile cv,
            @AuthenticationPrincipal UserDetails userDetails) {

        CandidatureRequest request = new CandidatureRequest();
        request.setLettreMotivation(lettreMotivation);
        request.setDisponibilite(java.time.LocalDate.parse(disponibilite));
        request.setTelephone(telephone);
        request.setNiveauEtudes(niveauEtudes);
        request.setEtablissement(etablissement);
        request.setLinkedinUrl(linkedinUrl);
        request.setPortfolioUrl(portfolioUrl);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(candidatureService.postuler(
                        offreId, request, cv, userDetails.getUsername()));
    }

    // ─── MES CANDIDATURES (existant) ─────────────────────────────────────
    @GetMapping("/mes-candidatures")
    public ResponseEntity<List<CandidatureResponse>> getMesCandidatures(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                candidatureService.getMesCandidatures(userDetails.getUsername()));
    }

    // ─── CANDIDATURES D'UNE OFFRE (existant) ─────────────────────────────
    @GetMapping("/offre/{offreId}")
    public ResponseEntity<List<CandidatureResponse>> getCandidaturesParOffre(
            @PathVariable Long offreId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                candidatureService.getCandidaturesParOffre(
                        offreId, userDetails.getUsername()));
    }

    // ─── TÉLÉCHARGER CV (existant) ───────────────────────────────────────
    @GetMapping("/{id}/cv")
    public ResponseEntity<Resource> downloadCv(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Path cvPath = candidatureService.getCvPath(id, userDetails.getUsername());
            Resource resource = new UrlResource(cvPath.toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + cvPath.getFileName() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ─── CHANGER STATUT (existant) ───────────────────────────────────────
    @PutMapping("/{id}/statut")
    public ResponseEntity<CandidatureResponse> changerStatut(
            @PathVariable Long id,
            @RequestBody StatutRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                candidatureService.changerStatut(
                        id, request, userDetails.getUsername()));
    }

    // ─── ANNULER (existant) ──────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> annuler(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        candidatureService.annuler(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    // ─── NOUVEAU : ACCEPTER AVEC FORMULAIRE DÉTAILLÉ ──────────────────────
    @PostMapping("/accepter")
    @PreAuthorize("hasRole('RECRUTEUR') or hasRole('ADMIN')")
    public ResponseEntity<CandidatureResponse> accepterCandidature(
            @RequestBody AcceptationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        CandidatureResponse response = candidatureService.accepterCandidature(request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    // ─── NOUVEAU : TÉLÉCHARGER L'INVITATION PDF (pour l'étudiant) ────────
    @GetMapping("/invitation/{candidatureId}/pdf")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<byte[]> telechargerInvitationPdf(
            @PathVariable Long candidatureId,
            @AuthenticationPrincipal UserDetails userDetails) {
        byte[] pdf = candidatureService.genererPdfInvitation(candidatureId, userDetails.getUsername());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invitation.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}