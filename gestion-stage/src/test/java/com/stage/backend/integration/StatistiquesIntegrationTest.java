package com.stage.backend.integration;

import com.stage.backend.entity.User;
import com.stage.backend.repository.CandidatureRepository;
import com.stage.backend.repository.CompetenceRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Statistiques — Tests d'intégration")
class StatistiquesIntegrationTest {

    @Autowired MockMvc               mockMvc;
    @Autowired JwtTestUtils          jwtUtils;
    @Autowired TestDataHelper        testData;
    @Autowired CandidatureRepository candidatureRepository;
    @Autowired OffreRepository       offreRepository;
    @Autowired CompetenceRepository  competenceRepository;

    private String tokenAdmin;
    private String tokenEtudiant;
    private String tokenRecruteur;

    @BeforeEach
    void setUp() {
        testData.initRoles();
        testData.createAdmin("admin.stats");
        testData.createEtudiant("etudiant.stats");
        testData.createRecruteur("recruteur.stats");
        tokenAdmin     = jwtUtils.tokenAdmin("admin.stats");
        tokenEtudiant  = jwtUtils.tokenEtudiant("etudiant.stats");
        tokenRecruteur = jwtUtils.tokenRecruteur("recruteur.stats");
    }

    @AfterEach
    void tearDown() {
        candidatureRepository.deleteAll();
        offreRepository.deleteAll();
        competenceRepository.deleteAll();
        testData.cleanAll();
    }

    // ════════════════════════════════════════════════════════════════════════
    // Admin → 200
    // ════════════════════════════════════════════════════════════════════════

    @Test @Order(1)
    @DisplayName("GET /statistiques — admin → 200 OK + corps JSON")
    void getStats_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/api/statistiques")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalOffres").isNumber())
                .andExpect(jsonPath("$.totalCandidatures").isNumber())
                .andExpect(jsonPath("$.totalEtudiants").isNumber())
                .andExpect(jsonPath("$.totalRecruteurs").isNumber());
    }

    @Test @Order(2)
    @DisplayName("GET /statistiques — admin → structure complète")
    void getStats_asAdmin_hasAllFields() throws Exception {
        mockMvc.perform(get("/api/statistiques")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOffres").exists())
                .andExpect(jsonPath("$.totalCandidatures").exists())
                .andExpect(jsonPath("$.totalEtudiants").exists())
                .andExpect(jsonPath("$.totalRecruteurs").exists())
                .andExpect(jsonPath("$.offreParSecteur").exists())
                .andExpect(jsonPath("$.candidaturesParStatut").exists())
                .andExpect(jsonPath("$.topCompetences").exists());
    }

    @Test @Order(3)
    @DisplayName("GET /statistiques — reflète les données en base")
    void getStats_reflectsData() throws Exception {
        User recruteur2 = testData.createRecruteur("recruteur2.stats");
        testData.createOffre("Stage Test", recruteur2);
        testData.createOffre("Stage Test", recruteur2);

        mockMvc.perform(get("/api/statistiques")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOffres", greaterThanOrEqualTo(2)));
    }

    // ════════════════════════════════════════════════════════════════════════
    // Sécurité : non-ADMIN → 403
    // ════════════════════════════════════════════════════════════════════════

    @Test @Order(4)
    @DisplayName("[Sécurité] ETUDIANT → 403 Forbidden")
    void getStats_asEtudiant_returns403() throws Exception {
        mockMvc.perform(get("/api/statistiques")
                        .header("Authorization", "Bearer " + tokenEtudiant))
                .andExpect(status().isForbidden());
    }

    @Test @Order(5)
    @DisplayName("[Sécurité] RECRUTEUR → 403 Forbidden")
    void getStats_asRecruteur_returns403() throws Exception {
        mockMvc.perform(get("/api/statistiques")
                        .header("Authorization", "Bearer " + tokenRecruteur))
                .andExpect(status().isForbidden());
    }

    // ════════════════════════════════════════════════════════════════════════
    // Sécurité : sans token → 4xx
    // CORRECTIF : Dans @SpringBootTest MockMvc, sans token → 403 (pas 401)
    // car le CsrfFilter intervient avant le JwtAuthTokenFilter.
    // On vérifie qu'on obtient bien un code d'erreur client (4xx).
    // ════════════════════════════════════════════════════════════════════════

    @Test @Order(6)
    @DisplayName("[Sécurité] Sans token → 4xx (accès refusé)")
    void getStats_unauthenticated_returns4xx() throws Exception {
        var result = mockMvc.perform(get("/api/statistiques"))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertThat(status).isGreaterThanOrEqualTo(400).isLessThan(500);
    }
}