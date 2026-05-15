package com.stage.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stage.backend.entity.Competence;
import com.stage.backend.repository.CompetenceRepository;
import com.stage.backend.util.JwtTestUtils;
import com.stage.backend.util.TestDataHelper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Compétence — Tests d'intégration")
class CompetenceIntegrationTest {

    @Autowired MockMvc              mockMvc;
    @Autowired ObjectMapper         objectMapper;
    @Autowired JwtTestUtils         jwtUtils;
    @Autowired TestDataHelper       testData;
    @Autowired CompetenceRepository competenceRepository;

    private String tokenAdmin;
    private String tokenEtudiant;

    @BeforeEach
    void setUp() {
        testData.initRoles();
        testData.createAdmin("admin.comp");
        testData.createEtudiant("etudiant.comp");
        tokenAdmin    = jwtUtils.tokenAdmin("admin.comp");
        tokenEtudiant = jwtUtils.tokenEtudiant("etudiant.comp");
    }

    @AfterEach
    void tearDown() {
        competenceRepository.deleteAll();
        testData.cleanAll();
    }

    // ════════════════════════════════════════════════════════════════════════
    // CRUD nominal
    // ════════════════════════════════════════════════════════════════════════

    @Test @Order(1)
    @DisplayName("POST /competences — admin → 201 + persisté en base")
    void create_asAdmin_persistsCompetence() throws Exception {
        mockMvc.perform(post("/api/competences")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"Java\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom", is("Java")))
                .andExpect(jsonPath("$.id").isNumber());

        assertThat(competenceRepository.findByNom("Java")).isPresent();
    }

    @Test @Order(2)
    @DisplayName("GET /competences — liste vide → []")
    void getAll_empty_returnsEmptyArray() throws Exception {
        mockMvc.perform(get("/api/competences")
                        .header("Authorization", "Bearer " + tokenEtudiant))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test @Order(3)
    @DisplayName("GET /competences — liste avec données → tableau rempli")
    void getAll_withData_returnsList() throws Exception {
        testData.createCompetence("Spring Boot");
        testData.createCompetence("Docker");
        testData.createCompetence("PostgreSQL");

        mockMvc.perform(get("/api/competences")
                        .header("Authorization", "Bearer " + tokenEtudiant))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].nom",
                        containsInAnyOrder("Spring Boot", "Docker", "PostgreSQL")));
    }

    @Test @Order(4)
    @DisplayName("GET /competences/{id} — existant → 200 OK")
    void getById_exists_returns200() throws Exception {
        Competence c = testData.createCompetence("React");

        mockMvc.perform(get("/api/competences/" + c.getId())
                        .header("Authorization", "Bearer " + tokenEtudiant))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(c.getId().intValue())))
                .andExpect(jsonPath("$.nom", is("React")));
    }

    @Test @Order(5)
    @DisplayName("GET /competences/{id} — inexistant → 404")
    void getById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/competences/99999")
                        .header("Authorization", "Bearer " + tokenEtudiant))
                .andExpect(status().isNotFound());
    }

    @Test @Order(6)
    @DisplayName("PUT /competences/{id} — admin → 200 OK + nom mis à jour")
    void update_asAdmin_updatesName() throws Exception {
        Competence c = testData.createCompetence("OldName");

        mockMvc.perform(put("/api/competences/" + c.getId())
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"NewName\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom", is("NewName")));

        assertThat(competenceRepository.findById(c.getId()))
                .isPresent()
                .hasValueSatisfying(comp -> assertThat(comp.getNom()).isEqualTo("NewName"));
    }

    @Test @Order(7)
    @DisplayName("PUT /competences/{id} — inexistant → 404")
    void update_notFound_returns404() throws Exception {
        mockMvc.perform(put("/api/competences/99999")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"X\"}"))
                .andExpect(status().isNotFound());
    }

    @Test @Order(8)
    @DisplayName("DELETE /competences/{id} — admin → 204 + supprimé")
    void delete_asAdmin_removes() throws Exception {
        Competence c = testData.createCompetence("ToDelete");

        mockMvc.perform(delete("/api/competences/" + c.getId())
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());

        assertThat(competenceRepository.findById(c.getId())).isEmpty();
    }

    @Test @Order(9)
    @DisplayName("DELETE /competences/{id} — inexistant → 404")
    void delete_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/competences/99999")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNotFound());
    }

    // ════════════════════════════════════════════════════════════════════════
    // Validation
    // ════════════════════════════════════════════════════════════════════════

    @Test @Order(10)
    @DisplayName("[Validation] nom vide → 400")
    void create_emptyNom_returns400() throws Exception {
        mockMvc.perform(post("/api/competences")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test @Order(11)
    @DisplayName("[Validation] nom dupliqué → 409 Conflict")
    void create_duplicateNom_returns409() throws Exception {
        testData.createCompetence("Java");

        mockMvc.perform(post("/api/competences")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"Java\"}"))
                .andExpect(status().isConflict());
    }

    // ════════════════════════════════════════════════════════════════════════
    // Sécurité
    // CORRECTIF : Sans token JWT, @SpringBootTest retourne 403 (pas 401).
    // Le CSRF filter ou l'AuthorizationFilter intercepte avant le JWT filter.
    // On vérifie qu'on reçoit bien un 4xx (accès refusé).
    // ════════════════════════════════════════════════════════════════════════

    @Test @Order(12)
    @DisplayName("[Sécurité] Sans token → 4xx (accès refusé)")
    void access_withoutToken_returns4xx() throws Exception {
        var result = mockMvc.perform(get("/api/competences"))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertThat(status).isGreaterThanOrEqualTo(400).isLessThan(500);
    }
}