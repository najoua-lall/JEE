package com.stage.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stage.backend.dto.OffreRequest;
import com.stage.backend.dto.OffreResponse;
import com.stage.backend.entity.Competence;
import com.stage.backend.entity.Offre;
import com.stage.backend.entity.User;
import com.stage.backend.security.JwtUtils;
import com.stage.backend.security.UserDetailsServiceImpl;
import com.stage.backend.service.OffreService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests boîte noire de OffreController via @WebMvcTest.
 *
 * CORRECTION : OffreResponse(Offre offre) est le seul constructeur disponible.
 * On construit donc un Offre factice complet pour créer les réponses de test.
 *
 * CORRECTION JSON : les strings malformées utilisent "{ \"titre\" : }"
 * pour éviter les warnings IDE sur les tokens non standard.
 */
@WebMvcTest(OffreController.class)
@DisplayName("OffreController — Tests boîte noire (@WebMvcTest)")
class OffreControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean OffreService offreService;
    @MockBean JwtUtils jwtUtils;
    @MockBean UserDetailsServiceImpl userDetailsService;

    // ── Helper : seul constructeur disponible est OffreResponse(Offre) ──────

    private OffreResponse fakeOffre(Long id, String titre) {
        User recruteur = new User();
        recruteur.setUsername("recruteur1");
        recruteur.setEmail("rec@test.com");
        recruteur.setPassword("encoded");

        Offre o = new Offre();
        o.setId(id);
        o.setTitre(titre);
        o.setDescription("Description test");
        o.setEntreprise("Acme Corp");
        o.setLocalisation("Rabat");
        o.setSecteur("IT");
        o.setDateDebut(LocalDate.of(2025, 6, 1));
        o.setDateFin(LocalDate.of(2025, 9, 1));
        o.setRecruteur(recruteur);
        o.setCompetences(new HashSet<>());
        return new OffreResponse(o);
    }

    private OffreRequest validRequest() {
        var r = new OffreRequest();
        r.setTitre("Stage Dev Java");
        r.setDescription("Développement backend Spring Boot");
        r.setEntreprise("Acme Corp");
        r.setLocalisation("Casablanca");
        r.setSecteur("IT");
        r.setDateDebut(LocalDate.of(2025, 6, 1));
        r.setDateFin(LocalDate.of(2025, 9, 1));
        return r;
    }

    // ════════════════════════════════════════════════════════════════════════
    // GET /api/offres → 200
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @WithMockUser
    @DisplayName("GET /api/offres → 200 OK + liste")
    void getAllOffres_returns200() throws Exception {
        when(offreService.getAllOffres())
                .thenReturn(List.of(fakeOffre(1L, "Stage Java"), fakeOffre(2L, "Stage React")));

        mockMvc.perform(get("/api/offres"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].titre", is("Stage Java")));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/offres/search → 200 OK + page")
    void searchOffres_returns200WithPage() throws Exception {
        var page = new PageImpl<>(
                List.of(fakeOffre(1L, "Stage Java")),
                PageRequest.of(0, 10), 1);
        when(offreService.searchOffres(any(), any(), any(), eq(0), eq(10)))
                .thenReturn(page);

        mockMvc.perform(get("/api/offres/search")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].titre", is("Stage Java")))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    // ════════════════════════════════════════════════════════════════════════
    // GET /api/offres/{id}
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @WithMockUser
    @DisplayName("GET /api/offres/{id} — existant → 200 OK")
    void getById_existing_returns200() throws Exception {
        when(offreService.getOffreById(1L)).thenReturn(fakeOffre(1L, "Stage Java"));

        mockMvc.perform(get("/api/offres/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.titre", is("Stage Java")));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/offres/{id} — ID inexistant → 404")
    void getById_notFound_returns404() throws Exception {
        when(offreService.getOffreById(999L))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "Offre introuvable (id=999)"));

        mockMvc.perform(get("/api/offres/999"))
                .andExpect(status().isNotFound());
    }

    // ════════════════════════════════════════════════════════════════════════
    // POST /api/offres → 201
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @WithMockUser(username = "recruteur1", roles = "RECRUTEUR")
    @DisplayName("POST /api/offres — valide → 201 Created")
    void createOffre_valid_returns201() throws Exception {
        var req  = validRequest();
        var resp = fakeOffre(5L, req.getTitre());
        when(offreService.createOffre(any(), eq("recruteur1"))).thenReturn(resp);

        mockMvc.perform(post("/api/offres")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.titre", is("Stage Dev Java")));
    }

    // ════════════════════════════════════════════════════════════════════════
    // POST invalide → 400
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @WithMockUser(roles = "RECRUTEUR")
    @DisplayName("POST /api/offres — titre manquant (@NotBlank) → 400")
    void createOffre_missingTitre_returns400() throws Exception {
        var req = validRequest();
        req.setTitre("");

        mockMvc.perform(post("/api/offres")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verify(offreService, never()).createOffre(any(), any());
    }

    @Test
    @WithMockUser(roles = "RECRUTEUR")
    @DisplayName("POST /api/offres — dateDebut null (@NotNull) → 400")
    void createOffre_nullDateDebut_returns400() throws Exception {
        var req = validRequest();
        req.setDateDebut(null);

        mockMvc.perform(post("/api/offres")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "RECRUTEUR")
    @DisplayName("POST /api/offres — entreprise vide (espaces) → 400")
    void createOffre_blankEntreprise_returns400() throws Exception {
        var req = validRequest();
        req.setEntreprise("   ");

        mockMvc.perform(post("/api/offres")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "RECRUTEUR")
    @DisplayName("POST /api/offres — JSON malformé → 400")
    void createOffre_malformedJson_returns400() throws Exception {
        // JSON invalide : valeur manquante après ":"
        String malformed = "{ \"titre\" : }";

        mockMvc.perform(post("/api/offres")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformed))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "RECRUTEUR")
    @DisplayName("POST /api/offres — corps vide → 400")
    void createOffre_emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/api/offres")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════════════════
    // PUT /api/offres/{id}
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @WithMockUser(username = "recruteur1", roles = "RECRUTEUR")
    @DisplayName("PUT /api/offres/{id} — valide → 200 OK")
    void updateOffre_valid_returns200() throws Exception {
        var req  = validRequest();
        req.setTitre("Stage Modifié");
        var resp = fakeOffre(1L, "Stage Modifié");

        when(offreService.updateOffre(eq(1L), any(), eq("recruteur1"))).thenReturn(resp);

        mockMvc.perform(put("/api/offres/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre", is("Stage Modifié")));
    }

    @Test
    @WithMockUser(username = "recruteur1", roles = "RECRUTEUR")
    @DisplayName("PUT /api/offres/{id} — ID inexistant → 404")
    void updateOffre_notFound_returns404() throws Exception {
        when(offreService.updateOffre(eq(999L), any(), any()))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "Offre introuvable (id=999)"));

        mockMvc.perform(put("/api/offres/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "autreRecruteur", roles = "RECRUTEUR")
    @DisplayName("PUT /api/offres/{id} — non propriétaire → 403 Forbidden")
    void updateOffre_notOwner_returns403() throws Exception {
        when(offreService.updateOffre(eq(1L), any(), eq("autreRecruteur")))
                .thenThrow(new ResponseStatusException(FORBIDDEN,
                        "Vous n'êtes pas le propriétaire de cette offre."));

        mockMvc.perform(put("/api/offres/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isForbidden());
    }

    // ════════════════════════════════════════════════════════════════════════
    // DELETE /api/offres/{id}
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @WithMockUser(username = "recruteur1", roles = "RECRUTEUR")
    @DisplayName("DELETE /api/offres/{id} — existant → 204 No Content")
    void deleteOffre_existing_returns204() throws Exception {
        doNothing().when(offreService).deleteOffre(eq(1L), eq("recruteur1"));

        mockMvc.perform(delete("/api/offres/1").with(csrf()))
                .andExpect(status().isNoContent());

        verify(offreService).deleteOffre(1L, "recruteur1");
    }

    @Test
    @WithMockUser(username = "recruteur1", roles = "RECRUTEUR")
    @DisplayName("DELETE /api/offres/{id} — ID inexistant → 404")
    void deleteOffre_notFound_returns404() throws Exception {
        doThrow(new ResponseStatusException(NOT_FOUND, "Offre introuvable (id=999)"))
                .when(offreService).deleteOffre(eq(999L), any());

        mockMvc.perform(delete("/api/offres/999").with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ════════════════════════════════════════════════════════════════════════
    // Sécurité
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("GET /api/offres — non authentifié → 401")
    void getAllOffres_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/offres"))
                .andExpect(status().isUnauthorized());
    }
}