package com.stage.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test de démarrage du contexte Spring.
 *
 * CORRECTIF : @ActiveProfiles("test") est OBLIGATOIRE.
 * Sans ce profil, Spring Boot essaie de se connecter à PostgreSQL
 * (configuré dans application.properties) au lieu d'utiliser H2.
 * Résultat sans le correctif : PSQLException Connection refused → ERROR.
 */
@SpringBootTest
@ActiveProfiles("test")
class GestionStageApplicationTests {

    @Test
    void contextLoads() {
        // Vérifie que le contexte Spring Boot démarre sans erreur avec H2.
    }
}