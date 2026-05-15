package com.stage.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stage.backend.dto.CompetenceRequest;
import com.stage.backend.dto.CompetenceResponse;
import com.stage.backend.entity.Competence;
import com.stage.backend.security.JwtUtils;
import com.stage.backend.security.UserDetailsServiceImpl;
import com.stage.backend.service.CompetenceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests boîte noire du CompetenceController via @WebMvcTest.
 *
 * CORRECTION : CompetenceResponse n'a qu'un seul constructeur :
 *   CompetenceResponse(Competence c)
 * On crée donc une Competence factice pour construire la réponse.
 *
 * CORRECTION JSON : les strings "{nom:}" et "{titre:}" sont remplacés par
 *   "{ \"nom\" : }" qui est du JSON invalide non ambigu pour l'IDE.
 */
@WebMvcTest(CompetenceController.class)
@DisplayName("CompetenceController — Tests boîte noire (@WebMvcTest)")
class CompetenceControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean CompetenceService competenceService;
    @MockBean JwtUtils jwtUtils;
    @MockBean UserDetailsServiceImpl userDetailsService;

    // ── Helper : seul constructeur disponible est CompetenceResponse(Competence) ──

    private CompetenceResponse fakeResponse(Long id, String nom) {
        Competence c = new Competence();
        c.setId(id);
        c.setNom(nom);
        return new CompetenceResponse(c);
    }

    // ════════════════════════════════════════════════════════════════════════
    // GET /api/competences → 200
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @WithMockUser
    @DisplayName("GET /api/competences → 200 OK + liste JSON")
    void getAll_returns200WithList() throws Exception {
        when(competenceService.getAll())
                .thenReturn(List.of(
                        fakeResponse(1L, "Java"),
                        fakeResponse(2L, "Spring Boot")));

        mockMvc.perform(get("/api/competences"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nom", is("Java")))
                .andExpect(jsonPath("$[1].nom", is("Spring Boot")));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/competences — liste vide → 200 OK avec tableau vide")
    void getAll_emptyList_returns200() throws Exception {
        when(competenceService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/competences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ════════════════════════════════════════════════════════════════════════
    // GET /api/competences/{id}
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @WithMockUser
    @DisplayName("GET /api/competences/{id} — existant → 200 OK")
    void getById_existing_returns200() throws Exception {
        when(competenceService.getById(1L)).thenReturn(fakeResponse(1L, "Java"));

        mockMvc.perform(get("/api/competences/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nom", is("Java")));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/competences/{id} — ID inexistant → 404 Not Found")
    void getById_notFound_returns404() throws Exception {
        when(competenceService.getById(999L))
                .thenThrow(new ResponseStatusException(NOT_FOUND,
                        "Compétence introuvable (id=999)"));

        mockMvc.perform(get("/api/competences/999"))
                .andExpect(status().isNotFound());
    }

    // ════════════════════════════════════════════════════════════════════════
    // POST /api/competences → 201 Created
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @WithMockUser
    @DisplayName("POST /api/competences — valide → 201 Created")
    void create_valid_returns201() throws Exception {
        var request = new CompetenceRequest();
        request.setNom("Docker");
        var response = fakeResponse(3L, "Docker");

        when(competenceService.create(any(CompetenceRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/competences")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.nom", is("Docker")));
    }

    // ════════════════════════════════════════════════════════════════════════
    // POST invalide → 400
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @WithMockUser
    @DisplayName("POST /api/competences — nom vide (@NotBlank) → 400")
    void create_blankNom_returns400() throws Exception {
        var request = new CompetenceRequest();
        request.setNom("");

        mockMvc.perform(post("/api/competences")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(competenceService, never()).create(any());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/competences — nom absent (objet vide) → 400")
    void create_nullNom_returns400() throws Exception {
        mockMvc.perform(post("/api/competences")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(competenceService, never()).create(any());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/competences — JSON malformé → 400")
    void create_malformedJson_returns400() throws Exception {
        // "{ \"nom\" : }" est du JSON syntaxiquement invalide (valeur manquante)
        // Cette forme évite le warning IDE sur les tokens non standard
        String malformed = "{ \"nom\" : }";

        mockMvc.perform(post("/api/competences")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformed))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/competences — corps vide → 400")
    void create_emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/api/competences")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/competences — doublon → 409 Conflict")
    void create_duplicate_returns409() throws Exception {
        var request = new CompetenceRequest();
        request.setNom("Java");

        when(competenceService.create(any()))
                .thenThrow(new ResponseStatusException(CONFLICT,
                        "Compétence déjà existante : Java"));

        mockMvc.perform(post("/api/competences")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // ════════════════════════════════════════════════════════════════════════
    // PUT /api/competences/{id}
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @WithMockUser
    @DisplayName("PUT /api/competences/{id} — valide → 200 OK")
    void update_valid_returns200() throws Exception {
        var request = new CompetenceRequest();
        request.setNom("Kubernetes");
        var response = fakeResponse(1L, "Kubernetes");

        when(competenceService.update(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/competences/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom", is("Kubernetes")));
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/competences/{id} — ID inexistant → 404")
    void update_notFound_returns404() throws Exception {
        var request = new CompetenceRequest();
        request.setNom("X");

        when(competenceService.update(eq(999L), any()))
                .thenThrow(new ResponseStatusException(NOT_FOUND,
                        "Compétence introuvable (id=999)"));

        mockMvc.perform(put("/api/competences/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ════════════════════════════════════════════════════════════════════════
    // DELETE /api/competences/{id}
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/competences/{id} — existant → 204 No Content")
    void delete_existing_returns204() throws Exception {
        doNothing().when(competenceService).delete(1L);

        mockMvc.perform(delete("/api/competences/1").with(csrf()))
                .andExpect(status().isNoContent());

        verify(competenceService).delete(1L);
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/competences/{id} — ID inexistant → 404")
    void delete_notFound_returns404() throws Exception {
        doThrow(new ResponseStatusException(NOT_FOUND, "Compétence introuvable (id=999)"))
                .when(competenceService).delete(999L);

        mockMvc.perform(delete("/api/competences/999").with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ════════════════════════════════════════════════════════════════════════
    // Sécurité
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("GET /api/competences — non authentifié → 401 Unauthorized")
    void getAll_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/competences"))
                .andExpect(status().isUnauthorized());
    }
}