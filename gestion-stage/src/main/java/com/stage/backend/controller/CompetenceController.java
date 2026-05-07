package com.stage.backend.controller;

import com.stage.backend.dto.CompetenceRequest;
import com.stage.backend.dto.CompetenceResponse;
import com.stage.backend.service.CompetenceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/competences")
@CrossOrigin(origins = "*")
public class CompetenceController {

    @Autowired
    private CompetenceService competenceService;

    @GetMapping
    public ResponseEntity<List<CompetenceResponse>> getAll() {
        return ResponseEntity.ok(competenceService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompetenceResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(competenceService.getById(id));
    }

    @PostMapping
    public ResponseEntity<CompetenceResponse> create(
            @Valid @RequestBody CompetenceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(competenceService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompetenceResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CompetenceRequest request) {
        return ResponseEntity.ok(competenceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        competenceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}