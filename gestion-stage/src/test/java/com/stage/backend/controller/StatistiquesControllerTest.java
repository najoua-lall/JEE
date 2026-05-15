package com.stage.backend.controller;

import com.stage.backend.config.WebSecurityTestConfig;
import com.stage.backend.dto.StatistiquesResponse;
import com.stage.backend.security.JwtUtils;
import com.stage.backend.security.UserDetailsServiceImpl;
import com.stage.backend.service.StatistiquesService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests boîte noire de StatistiquesController via @WebMvcTest.
 *
 * CORRECTIF :
 * 1. @Import(WebSecurityTestConfig.class) pour charger la config de sécurité
 *    correcte qui applique hasRole("ADMIN") sur /api/statistiques/**.
 *    Sans cela, la config par défaut ne lit pas @PreAuthorize et renvoie
 *    200 pour tout utilisateur authentifié.
 *
 * 2. Les tests 403 (ETUDIANT/RECRUTEUR) fonctionnent car WebSecurityTestConfig
 *    déclare .hasRole("ADMIN") sur /api/statistiques/**.
 */
@WebMvcTest(StatistiquesController.class)
@Import(WebSecurityTestConfig.class)
@DisplayName("StatistiquesController — Tests boîte noire (@WebMvcTest)")
class StatistiquesControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean StatistiquesService statistiquesService;
    @MockBean JwtUtils jwtUtils;
    @MockBean UserDetailsServiceImpl userDetailsService;

    private StatistiquesResponse fakeStats() {
        var s = new StatistiquesResponse();
        s.setTotalOffres(10L);
        s.setTotalCandidatures(25L);
        s.setTotalEtudiants(15L);
        s.setTotalRecruteurs(5L);
        s.setOffreParSecteur(Map.of("IT", 7L, "Finance", 3L));
        s.setTopCompetences(List.of(
                new StatistiquesResponse.CompetenceStat("Java", 5L),
                new StatistiquesResponse.CompetenceStat("React", 4L)));
        s.setCandidaturesParStatut(Map.of(
                "EN_ATTENTE", 10L,
                "ACCEPTEE", 8L,
                "REFUSEE", 7L));
        return s;
    }

    // ════════════════════════════════════════════════════════════════════════
    // GET → 200 (ADMIN)
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/statistiques — ADMIN → 200 OK + données")
    void getStats_asAdmin_returns200() throws Exception {
        when(statistiquesService.getStatistiques()).thenReturn(fakeStats());

        mockMvc.perform(get("/api/statistiques"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalOffres", is(10)))
                .andExpect(jsonPath("$.totalCandidatures", is(25)))
                .andExpect(jsonPath("$.totalEtudiants", is(15)))
                .andExpect(jsonPath("$.totalRecruteurs", is(5)))
                .andExpect(jsonPath("$.offreParSecteur.IT", is(7)))
                .andExpect(jsonPath("$.topCompetences", hasSize(2)))
                .andExpect(jsonPath("$.topCompetences[0].nom", is("Java")))
                .andExpect(jsonPath("$.candidaturesParStatut.ACCEPTEE", is(8)));
    }

    // ════════════════════════════════════════════════════════════════════════
    // Sécurité : non-ADMIN → 403
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @WithMockUser(roles = "ETUDIANT")
    @DisplayName("GET /api/statistiques — ETUDIANT → 403 Forbidden")
    void getStats_asEtudiant_returns403() throws Exception {
        mockMvc.perform(get("/api/statistiques"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "RECRUTEUR")
    @DisplayName("GET /api/statistiques — RECRUTEUR → 403 Forbidden")
    void getStats_asRecruteur_returns403() throws Exception {
        mockMvc.perform(get("/api/statistiques"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/statistiques — non authentifié → 401")
    void getStats_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/statistiques"))
                .andExpect(status().isUnauthorized());
    }

    // ════════════════════════════════════════════════════════════════════════
    // REST Contract
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("[Contract] Réponse contient tous les champs obligatoires")
    void getStats_responseContainsAllFields() throws Exception {
        when(statistiquesService.getStatistiques()).thenReturn(fakeStats());

        mockMvc.perform(get("/api/statistiques"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOffres").isNumber())
                .andExpect(jsonPath("$.totalCandidatures").isNumber())
                .andExpect(jsonPath("$.totalEtudiants").isNumber())
                .andExpect(jsonPath("$.totalRecruteurs").isNumber())
                .andExpect(jsonPath("$.offreParSecteur").isMap())
                .andExpect(jsonPath("$.candidaturesParStatut").isMap())
                .andExpect(jsonPath("$.topCompetences").isArray())
                .andExpect(jsonPath("$.topCompetences[0].nom").isString())
                .andExpect(jsonPath("$.topCompetences[0].count").isNumber());
    }
}