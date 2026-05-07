package com.stage.backend.service;

import com.stage.backend.dto.OffreRequest;
import com.stage.backend.dto.OffreResponse;
import com.stage.backend.entity.Competence;
import com.stage.backend.entity.ERole;
import com.stage.backend.entity.Offre;
import com.stage.backend.entity.User;
import com.stage.backend.repository.CompetenceRepository;
import com.stage.backend.repository.OffreRepository;
import com.stage.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OffreService {

    @Autowired private OffreRepository offreRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CompetenceRepository competenceRepository;

    public List<OffreResponse> getAllOffres() {
        return offreRepository.findAll()
                .stream()
                .map(OffreResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Recherche pour un étudiant (toutes les offres).
     */
    public Page<OffreResponse> searchOffres(
            String search, String secteur, String competenceNom,
            int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return offreRepository
                .searchOffres(
                        nullIfBlank(search),
                        nullIfBlank(secteur),
                        nullIfBlank(competenceNom),
                        pageable)
                .map(OffreResponse::new);
    }

    /**
     * Recherche filtrée : un recruteur ne voit que ses propres offres.
     */
    public Page<OffreResponse> searchOffresParRecruteur(
            String username,
            String search, String secteur, String competenceNom,
            int page, int size) {

        User recruteur = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return offreRepository
                .searchOffresByRecruteur(
                        recruteur.getId(),
                        nullIfBlank(search),
                        nullIfBlank(secteur),
                        nullIfBlank(competenceNom),
                        pageable)
                .map(OffreResponse::new);
    }

    public OffreResponse getOffreById(Long id) {
        return new OffreResponse(findOffre(id));
    }

    @Transactional
    public OffreResponse createOffre(OffreRequest request, String username) {
        User recruteur = getRecruteur(username);
        Offre offre = new Offre();
        mapRequestToOffre(request, offre);
        offre.setRecruteur(recruteur);
        return new OffreResponse(offreRepository.save(offre));
    }

    @Transactional
    public OffreResponse updateOffre(Long id, OffreRequest request, String username) {
        Offre offre = findOffre(id);
        checkOwnership(offre, username);
        mapRequestToOffre(request, offre);
        return new OffreResponse(offreRepository.save(offre));
    }

    @Transactional
    public void deleteOffre(Long id, String username) {
        Offre offre = findOffre(id);
        checkOwnership(offre, username);
        offreRepository.delete(offre);
    }

    @Transactional
    public OffreResponse associerCompetences(Long offreId, Set<Long> competenceIds, String username) {
        Offre offre = findOffre(offreId);
        checkOwnership(offre, username);
        Set<Competence> competences = new HashSet<>(competenceRepository.findAllById(competenceIds));
        offre.setCompetences(competences);
        return new OffreResponse(offreRepository.save(offre));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Offre findOffre(Long id) {
        return offreRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Offre introuvable (id=" + id + ")"));
    }

    private User getRecruteur(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        boolean isRecruteur = user.getRoles().stream()
                .anyMatch(r -> r.getName() == ERole.ROLE_RECRUTEUR
                        || r.getName() == ERole.ROLE_ADMIN);
        if (!isRecruteur) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Seul un recruteur peut gérer des offres.");
        }
        return user;
    }

    private void checkOwnership(Offre offre, String username) {
        if (!offre.getRecruteur().getUsername().equals(username)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Vous n'êtes pas le propriétaire de cette offre.");
        }
    }

    private void mapRequestToOffre(OffreRequest request, Offre offre) {
        offre.setTitre(request.getTitre());
        offre.setDescription(request.getDescription());
        offre.setEntreprise(request.getEntreprise());
        offre.setLocalisation(request.getLocalisation());
        offre.setSecteur(request.getSecteur());
        offre.setDateDebut(request.getDateDebut());
        offre.setDateFin(request.getDateFin());

        if (request.getCompetenceIds() != null && !request.getCompetenceIds().isEmpty()) {
            Set<Competence> competences = new HashSet<>(
                    competenceRepository.findAllById(request.getCompetenceIds()));
            offre.setCompetences(competences);
        }
    }

    private static String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}