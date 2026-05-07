package com.stage.backend.service;

import com.stage.backend.dto.CompetenceRequest;
import com.stage.backend.dto.CompetenceResponse;
import com.stage.backend.entity.Competence;
import com.stage.backend.repository.CompetenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompetenceService {

    @Autowired
    private CompetenceRepository competenceRepository;

    public List<CompetenceResponse> getAll() {
        return competenceRepository.findAll()
                .stream()
                .map(CompetenceResponse::new)
                .collect(Collectors.toList());
    }

    public CompetenceResponse getById(Long id) {
        return new CompetenceResponse(findById(id));
    }

    @Transactional
    public CompetenceResponse create(CompetenceRequest request) {
        if (competenceRepository.existsByNom(request.getNom())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Compétence déjà existante : " + request.getNom());
        }
        Competence c = new Competence();
        c.setNom(request.getNom());
        return new CompetenceResponse(competenceRepository.save(c));
    }

    @Transactional
    public CompetenceResponse update(Long id, CompetenceRequest request) {
        Competence c = findById(id);
        c.setNom(request.getNom());
        return new CompetenceResponse(competenceRepository.save(c));
    }

    @Transactional
    public void delete(Long id) {
        competenceRepository.delete(findById(id));
    }

    private Competence findById(Long id) {
        return competenceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Compétence introuvable (id=" + id + ")"));
    }
}