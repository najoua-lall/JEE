package com.stage.backend.controller;

import com.stage.backend.dto.StatistiquesResponse;
import com.stage.backend.service.StatistiquesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statistiques")
@CrossOrigin(origins = "*")
public class StatistiquesController {

    @Autowired
    private StatistiquesService statistiquesService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<StatistiquesResponse> getStatistiques() {
        return ResponseEntity.ok(statistiquesService.getStatistiques());
    }
}