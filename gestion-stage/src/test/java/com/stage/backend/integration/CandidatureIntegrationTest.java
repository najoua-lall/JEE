package com.stage.backend.integration;

import com.stage.backend.entity.Candidature;
import com.stage.backend.entity.Offre;
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
import org.springframework.mock.web.MockMultipartFile;
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
@DisplayName("Candidature — Tests d'intégration")
class CandidatureIntegrationTest {

    @Autowired MockMvc               mockMvc;
    @Autowired JwtTestUtils          jwtUtils;
    @Autowired TestDataHelper        testData;
    @Autowired CandidatureRepository candidatureRepository;
    @Autowired OffreRepository       offreRepository;

    private User   etudiant;
    private User   recruteur;
    private Offre  offre;
    private String tokenEtudiant;
    private String tokenRecruteur;
    private String tokenAdmin;

    @BeforeEach
    void setUp() {
        testData.initRoles();
        etudiant   = testData.createEtudiant("etudiant.cand");
        recruteur  = testData.createRecruteur("recruteur.cand");
        testData.createAdmin("admin.cand");
        offre          = testData.createOffre("Stage ENSAM", recruteur);
        tokenEtudiant  = jwtUtils.tokenEtudiant("etudiant.cand");
        tokenRecruteur = jwtUtils.tokenRecruteur("recruteur.cand");
        tokenAdmin     = jwtUtils.tokenAdmin("admin.cand");
    }

    @AfterEach
    void tearDown() {
        candidatureRepository.deleteAll();
        offreRepository.deleteAll();
        testData.cleanAll();
    }

    // ── Helper : fichier CV factice ──────────────────────────────────────────
    // CORRECTIF : Le controller postuler() requiert un fichier CV (@RequestPart("cv")).
    // Sans ce fichier, Spring retourne 400 "Required part 'cv' is not present."
    // On crée un MockMultipartFile vide pour satisfaire la contrainte.

    private MockMultipartFile fakeCv() {
        return new MockMultipartFile(
                "cv",
                "cv.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "fake-cv-content".getBytes()
        );
    }

    // ════════════════════════════════════════════════════════════════════════
    // Postuler
    // ════════════════════════════════════════════════════════════════════════

    @Test @Order(1)
    @DisplayName("POST /postuler/{offreId} — étudiant + CV → 201 Created")
    void postuler_valid_returns201() throws Exception {
        mockMvc.perform(multipart("/api/candidatures/postuler/" + offre.getId())
                        .file(fakeCv())
                        .header("Authorization", "Bearer " + tokenEtudiant)
                        .param("lettreMotivation", "Je suis motivé.")
                        .param("disponibilite", "2025-06-01")
                        .param("telephone", "+212677777777")
                        .param("niveauEtudes", "Bac+5")
                        .param("etablissement", "ENSAM"))
                .andExpect(status().isCreated());

        assertThat(candidatureRepository.findAll()).hasSize(1);
    }

    @Test @Order(2)
    @DisplayName("POST /postuler/{offreId} — doublon → 409 Conflict")
    void postuler_duplicate_returns409() throws Exception {
        testData.createCandidature(etudiant, offre);

        mockMvc.perform(multipart("/api/candidatures/postuler/" + offre.getId())
                        .file(fakeCv())
                        .header("Authorization", "Bearer " + tokenEtudiant)
                        .param("lettreMotivation", "Re-candidature")
                        .param("disponibilite", "2025-06-01")
                        .param("telephone", "+212677777777")
                        .param("niveauEtudes", "Bac+5")
                        .param("etablissement", "ENSAM"))
                .andExpect(status().isConflict());
    }

    @Test @Order(3)
    @DisplayName("POST /postuler/{offreId} — recruteur interdit → 403")
    void postuler_asRecruteur_returns403() throws Exception {
        mockMvc.perform(multipart("/api/candidatures/postuler/" + offre.getId())
                        .file(fakeCv())
                        .header("Authorization", "Bearer " + tokenRecruteur)
                        .param("lettreMotivation", "Candidature")
                        .param("disponibilite", "2025-06-01")
                        .param("telephone", "+212677777777")
                        .param("niveauEtudes", "Bac+5")
                        .param("etablissement", "ENSAM"))
                .andExpect(status().isForbidden());
    }

    @Test @Order(4)
    @DisplayName("POST /postuler/{offreId} — offre inexistante → 404")
    void postuler_offreNotFound_returns404() throws Exception {
        mockMvc.perform(multipart("/api/candidatures/postuler/99999")
                        .file(fakeCv())
                        .header("Authorization", "Bearer " + tokenEtudiant)
                        .param("lettreMotivation", "Candidature")
                        .param("disponibilite", "2025-06-01")
                        .param("telephone", "+212677777777")
                        .param("niveauEtudes", "Bac+5")
                        .param("etablissement", "ENSAM"))
                .andExpect(status().isNotFound());
    }

    // ════════════════════════════════════════════════════════════════════════
    // Mes candidatures
    // ════════════════════════════════════════════════════════════════════════

    @Test @Order(5)
    @DisplayName("GET /mes-candidatures — liste propre à l'étudiant")
    void mesCandidatures_returnsOwn() throws Exception {
        testData.createCandidature(etudiant, offre);

        User autreEtudiant = testData.createEtudiant("autre.etudiant");
        Offre autreOffre   = testData.createOffre("Autre Stage", recruteur);
        testData.createCandidature(autreEtudiant, autreOffre);

        mockMvc.perform(get("/api/candidatures/mes-candidatures")
                        .header("Authorization", "Bearer " + tokenEtudiant))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test @Order(6)
    @DisplayName("GET /mes-candidatures — liste vide")
    void mesCandidatures_empty() throws Exception {
        mockMvc.perform(get("/api/candidatures/mes-candidatures")
                        .header("Authorization", "Bearer " + tokenEtudiant))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ════════════════════════════════════════════════════════════════════════
    // Candidatures par offre (recruteur)
    // ════════════════════════════════════════════════════════════════════════

    @Test @Order(7)
    @DisplayName("GET /offre/{offreId} — recruteur voit ses candidatures")
    void candidaturesParOffre_asRecruteur() throws Exception {
        testData.createCandidature(etudiant, offre);

        mockMvc.perform(get("/api/candidatures/offre/" + offre.getId())
                        .header("Authorization", "Bearer " + tokenRecruteur))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test @Order(8)
    @DisplayName("GET /offre/{offreId} — étudiant interdit → 403")
    void candidaturesParOffre_asEtudiant_returns403() throws Exception {
        mockMvc.perform(get("/api/candidatures/offre/" + offre.getId())
                        .header("Authorization", "Bearer " + tokenEtudiant))
                .andExpect(status().isForbidden());
    }

    // ════════════════════════════════════════════════════════════════════════
    // Changer statut
    // ════════════════════════════════════════════════════════════════════════

    @Test @Order(9)
    @DisplayName("PUT /{id}/statut — recruteur accepte → 200")
    void changerStatut_recruteurAccepte_returns200() throws Exception {
        Candidature c = testData.createCandidature(etudiant, offre);

        mockMvc.perform(put("/api/candidatures/" + c.getId() + "/statut")
                        .header("Authorization", "Bearer " + tokenRecruteur)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statut\":\"ACCEPTEE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut", is("ACCEPTEE")));
    }

    @Test @Order(10)
    @DisplayName("PUT /{id}/statut — recruteur refuse → 200")
    void changerStatut_recruteurRefuse_returns200() throws Exception {
        Candidature c = testData.createCandidature(etudiant, offre);

        mockMvc.perform(put("/api/candidatures/" + c.getId() + "/statut")
                        .header("Authorization", "Bearer " + tokenRecruteur)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statut\":\"REFUSEE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut", is("REFUSEE")));
    }

    @Test @Order(11)
    @DisplayName("PUT /{id}/statut — étudiant interdit → 403")
    void changerStatut_asEtudiant_returns403() throws Exception {
        Candidature c = testData.createCandidature(etudiant, offre);

        mockMvc.perform(put("/api/candidatures/" + c.getId() + "/statut")
                        .header("Authorization", "Bearer " + tokenEtudiant)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statut\":\"ACCEPTEE\"}"))
                .andExpect(status().isForbidden());
    }

    @Test @Order(12)
    @DisplayName("PUT /{id}/statut — candidature inexistante → 404")
    void changerStatut_notFound_returns404() throws Exception {
        mockMvc.perform(put("/api/candidatures/99999/statut")
                        .header("Authorization", "Bearer " + tokenRecruteur)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statut\":\"ACCEPTEE\"}"))
                .andExpect(status().isNotFound());
    }

    @Test @Order(13)
    @DisplayName("DELETE /{id} — étudiant retire sa candidature → 204")
    void delete_asEtudiant_returns204() throws Exception {
        Candidature c = testData.createCandidature(etudiant, offre);

        mockMvc.perform(delete("/api/candidatures/" + c.getId())
                        .header("Authorization", "Bearer " + tokenEtudiant))
                .andExpect(status().isNoContent());

        assertThat(candidatureRepository.findById(c.getId())).isEmpty();
    }

    // ════════════════════════════════════════════════════════════════════════
    // Sécurité
    // ════════════════════════════════════════════════════════════════════════

    @Test @Order(14)
    @DisplayName("[Sécurité] Sans token → 4xx (accès refusé)")
    void postuler_unauthenticated_returns4xx() throws Exception {
        // Sans token JWT dans @SpringBootTest MockMvc → 403 (CSRF absent)
        var result = mockMvc.perform(
                        multipart("/api/candidatures/postuler/" + offre.getId())
                                .file(fakeCv())
                                .param("lettreMotivation", "Lettre.")
                                .param("disponibilite", "2025-06-01")
                                .param("telephone", "+212677777777")
                                .param("niveauEtudes", "Bac+5")
                                .param("etablissement", "ENSAM"))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertThat(status).isGreaterThanOrEqualTo(400).isLessThan(500);
    }
}