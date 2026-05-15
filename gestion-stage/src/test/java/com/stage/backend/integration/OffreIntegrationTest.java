package com.stage.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stage.backend.dto.OffreRequest;
import com.stage.backend.entity.User;
import com.stage.backend.repository.CandidatureRepository;
import com.stage.backend.repository.OffreRepository;
import com.stage.backend.util.JwtTestUtils;
import com.stage.backend.util.TestDataHelper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * Tests d'intégration Offres : @SpringBootTest + H2 + JWT réel.
 * ─────────────────────────────────────────────────────────────────────────────
 * Couvre :
 *  3. Integration : Create→Read, Update→Read, Delete→404
 *  5. E2E : Create→List→Update→Get→Delete→404
 *  6. Cas limites : ID inexistant, non propriétaire, données invalides
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Offres — Tests d'intégration complets")
class OffreIntegrationTest {

    @Autowired MockMvc            mockMvc;
    @Autowired ObjectMapper       objectMapper;
    @Autowired JwtTestUtils       jwtUtils;
    @Autowired TestDataHelper     testData;
    @Autowired OffreRepository    offreRepository;
    @Autowired CandidatureRepository candidatureRepository;

    private String tokenRecruteur;
    private String tokenAutreRecruteur;
    private String tokenEtudiant;
    private User   recruteur;

    @BeforeEach
    void setUp() {
        testData.initRoles();
        recruteur             = testData.createRecruteur("recruteur.offre");
        testData.createRecruteur("autre.recruteur");
        testData.createEtudiant("etudiant.offre");

        tokenRecruteur      = jwtUtils.tokenRecruteur("recruteur.offre");
        tokenAutreRecruteur = jwtUtils.tokenRecruteur("autre.recruteur");
        tokenEtudiant       = jwtUtils.tokenEtudiant("etudiant.offre");
    }

    @AfterEach
    void tearDown() {
        candidatureRepository.deleteAll();
        offreRepository.deleteAll();
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private OffreRequest validRequest(String titre) {
        var r = new OffreRequest();
        r.setTitre(titre);
        r.setDescription("Description complète pour le test d'intégration.");
        r.setEntreprise("TechCorp SA");
        r.setLocalisation("Casablanca");
        r.setSecteur("IT");
        r.setDateDebut(LocalDate.of(2025, 6, 1));
        r.setDateFin(LocalDate.of(2025, 9, 30));
        return r;
    }

    // ════════════════════════════════════════════════════════════════════════
    // 3. Integration : Create → Read
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("[Intégration] POST offre puis GET → offre persistée")
    void create_thenRead_persistedCorrectly() throws Exception {
        // Create
        var result = mockMvc.perform(post("/api/offres")
                        .header("Authorization", "Bearer " + tokenRecruteur)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("Stage Backend Java"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titre", is("Stage Backend Java")))
                .andExpect(jsonPath("$.recruteurUsername", is("recruteur.offre")))
                .andReturn();

        long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // Read
        mockMvc.perform(get("/api/offres/" + id)
                        .header("Authorization", "Bearer " + tokenEtudiant))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is((int) id)))
                .andExpect(jsonPath("$.titre", is("Stage Backend Java")));

        assertThat(offreRepository.findById(id)).isPresent();
    }

    // ════════════════════════════════════════════════════════════════════════
    // 3. Integration : Update → Read
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @Order(2)
    @DisplayName("[Intégration] POST puis PUT puis GET → modification visible")
    void create_update_thenRead_showsUpdate() throws Exception {
        var result = mockMvc.perform(post("/api/offres")
                        .header("Authorization", "Bearer " + tokenRecruteur)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("Stage Initial"))))
                .andExpect(status().isCreated())
                .andReturn();
        long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // Update
        var updateReq = validRequest("Stage Modifié");
        updateReq.setDescription("Nouvelle description mise à jour.");
        mockMvc.perform(put("/api/offres/" + id)
                        .header("Authorization", "Bearer " + tokenRecruteur)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre", is("Stage Modifié")));

        // Read
        mockMvc.perform(get("/api/offres/" + id)
                        .header("Authorization", "Bearer " + tokenEtudiant))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre", is("Stage Modifié")))
                .andExpect(jsonPath("$.description", containsString("Nouvelle description")));
    }

    // ════════════════════════════════════════════════════════════════════════
    // 3. Integration : Delete → 404
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @Order(3)
    @DisplayName("[Intégration] POST puis DELETE puis GET → 404")
    void create_delete_thenGet_returns404() throws Exception {
        var result = mockMvc.perform(post("/api/offres")
                        .header("Authorization", "Bearer " + tokenRecruteur)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("Stage À Supprimer"))))
                .andExpect(status().isCreated())
                .andReturn();
        long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // Delete
        mockMvc.perform(delete("/api/offres/" + id)
                        .header("Authorization", "Bearer " + tokenRecruteur))
                .andExpect(status().isNoContent());

        // Get → 404
        mockMvc.perform(get("/api/offres/" + id)
                        .header("Authorization", "Bearer " + tokenEtudiant))
                .andExpect(status().isNotFound());

        assertThat(offreRepository.findById(id)).isEmpty();
    }

    // ════════════════════════════════════════════════════════════════════════
    // 5. E2E : Create → List → Update → Get → Delete → 404
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @Order(4)
    @DisplayName("[E2E] Flux complet Offre : Create→List→Update→Get→Delete→404")
    void e2e_fullLifecycle() throws Exception {
        // 1. Create
        var result = mockMvc.perform(post("/api/offres")
                        .header("Authorization", "Bearer " + tokenRecruteur)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("Stage E2E"))))
                .andExpect(status().isCreated())
                .andReturn();
        long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // 2. List (tous — endpoint étudiant)
        mockMvc.perform(get("/api/offres/search")
                        .header("Authorization", "Bearer " + tokenEtudiant)
                        .param("search", "Stage E2E"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.titre=='Stage E2E')]").exists());

        // 3. Update
        var updateReq = validRequest("Stage E2E Updated");
        mockMvc.perform(put("/api/offres/" + id)
                        .header("Authorization", "Bearer " + tokenRecruteur)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre", is("Stage E2E Updated")));

        // 4. Get
        mockMvc.perform(get("/api/offres/" + id)
                        .header("Authorization", "Bearer " + tokenEtudiant))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre", is("Stage E2E Updated")));

        // 5. Delete
        mockMvc.perform(delete("/api/offres/" + id)
                        .header("Authorization", "Bearer " + tokenRecruteur))
                .andExpect(status().isNoContent());

        // 6. Get → 404
        mockMvc.perform(get("/api/offres/" + id)
                        .header("Authorization", "Bearer " + tokenEtudiant))
                .andExpect(status().isNotFound());
    }

    // ════════════════════════════════════════════════════════════════════════
    // 6. Cas limites
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @Order(5)
    @DisplayName("[Limite] GET ID inexistant → 404")
    void get_nonExistent_returns404() throws Exception {
        mockMvc.perform(get("/api/offres/99999")
                        .header("Authorization", "Bearer " + tokenEtudiant))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(6)
    @DisplayName("[Limite] DELETE ID inexistant → 404")
    void delete_nonExistent_returns404() throws Exception {
        mockMvc.perform(delete("/api/offres/99999")
                        .header("Authorization", "Bearer " + tokenRecruteur))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(7)
    @DisplayName("[Limite] PUT par non-propriétaire → 403 Forbidden")
    void update_byOtherRecruteur_returns403() throws Exception {
        var result = mockMvc.perform(post("/api/offres")
                        .header("Authorization", "Bearer " + tokenRecruteur)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("Stage Protégé"))))
                .andExpect(status().isCreated())
                .andReturn();
        long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(put("/api/offres/" + id)
                        .header("Authorization", "Bearer " + tokenAutreRecruteur)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("Tentative Modification"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(8)
    @DisplayName("[Limite] DELETE deux fois → 2ème retourne 404")
    void delete_twice_secondReturns404() throws Exception {
        var result = mockMvc.perform(post("/api/offres")
                        .header("Authorization", "Bearer " + tokenRecruteur)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("Stage Double Delete"))))
                .andExpect(status().isCreated())
                .andReturn();
        long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/offres/" + id)
                        .header("Authorization", "Bearer " + tokenRecruteur))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/offres/" + id)
                        .header("Authorization", "Bearer " + tokenRecruteur))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(9)
    @DisplayName("[Limite] POST — titre manquant → 400 Bad Request")
    void create_missingTitre_returns400() throws Exception {
        var req = validRequest("X");
        req.setTitre("");

        mockMvc.perform(post("/api/offres")
                        .header("Authorization", "Bearer " + tokenRecruteur)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(10)
    @DisplayName("[Limite] POST — étudiant ne peut pas créer d'offre → 403")
    void create_byEtudiant_returns403() throws Exception {
        mockMvc.perform(post("/api/offres")
                        .header("Authorization", "Bearer " + tokenEtudiant)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("Stage Illicite"))))
                .andExpect(status().isForbidden());
    }

    // ════════════════════════════════════════════════════════════════════════
    // 4. REST Contract
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @Order(11)
    @DisplayName("[Contract] Réponse POST inclut Content-Type application/json")
    void create_responseContentTypeIsJson() throws Exception {
        mockMvc.perform(post("/api/offres")
                        .header("Authorization", "Bearer " + tokenRecruteur)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("Stage Contract"))))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.titre").value("Stage Contract"))
                .andExpect(jsonPath("$.recruteurUsername").value("recruteur.offre"));
    }
}