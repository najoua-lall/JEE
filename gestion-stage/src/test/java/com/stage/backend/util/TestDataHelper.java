package com.stage.backend.util;

import com.stage.backend.entity.*;
import com.stage.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

/**
 * Utilitaire partagé pour créer des données de test cohérentes.
 * Utilisé par les tests d'intégration (@SpringBootTest).
 */
@Component
public class TestDataHelper {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private OffreRepository offreRepository;
    @Autowired private CompetenceRepository competenceRepository;
    @Autowired private CandidatureRepository candidatureRepository;
    @Autowired private InvitationRepository invitationRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // ── Rôles ────────────────────────────────────────────────────────────────

    public Role getOrCreateRole(ERole eRole) {
        return roleRepository.findByName(eRole).orElseGet(() -> {
            Role r = new Role();
            r.setName(eRole);
            return roleRepository.save(r);
        });
    }

    public void initRoles() {
        for (ERole r : ERole.values()) getOrCreateRole(r);
    }

    // ── Utilisateurs ─────────────────────────────────────────────────────────

    public User createRecruteur(String username) {
        return createUser(username, username + "@test.com", ERole.ROLE_RECRUTEUR);
    }

    public User createEtudiant(String username) {
        return createUser(username, username + "@test.com", ERole.ROLE_ETUDIANT);
    }

    public User createAdmin(String username) {
        return createUser(username, username + "@test.com", ERole.ROLE_ADMIN);
    }

    public User createUser(String username, String email, ERole eRole) {
        // Évite les doublons si le test est relancé sans rollback complet
        return userRepository.findByUsername(username).orElseGet(() -> {
            User u = new User();
            u.setUsername(username);
            u.setEmail(email);
            u.setPassword(passwordEncoder.encode("password123"));
            u.setRoles(Set.of(getOrCreateRole(eRole)));
            return userRepository.save(u);
        });
    }

    // ── Offres ───────────────────────────────────────────────────────────────

    public Offre createOffre(String titre, User recruteur) {
        Offre o = new Offre();
        o.setTitre(titre);
        o.setDescription("Description de " + titre);
        o.setEntreprise("Entreprise Test");
        o.setLocalisation("Casablanca");
        o.setSecteur("IT");
        o.setDateDebut(LocalDate.now().plusDays(10));
        o.setDateFin(LocalDate.now().plusMonths(3));
        o.setRecruteur(recruteur);
        return offreRepository.save(o);
    }

    // ── Compétences ──────────────────────────────────────────────────────────

    public Competence createCompetence(String nom) {
        return competenceRepository.findByNom(nom).orElseGet(() -> {
            Competence c = new Competence();
            c.setNom(nom);
            return competenceRepository.save(c);
        });
    }

    // ── Candidatures ─────────────────────────────────────────────────────────

    public Candidature createCandidature(User etudiant, Offre offre) {
        Candidature c = new Candidature();
        c.setEtudiant(etudiant);
        c.setOffre(offre);
        c.setStatut(StatutCandidature.EN_ATTENTE);
        c.setLettreMotivation("Je suis très motivé pour rejoindre votre équipe.");
        c.setCvFileName("cv_test.pdf");
        c.setCvFilePath("/tmp/cv_test.pdf");
        c.setDisponibilite(LocalDate.now().plusDays(30));
        c.setTelephone("+212600000000");
        c.setNiveauEtudes("Bac+5");
        c.setEtablissement("ENSAM Meknès");
        return candidatureRepository.save(c);
    }

    // ── Nettoyage ────────────────────────────────────────────────────────────

    public void cleanAll() {
        invitationRepository.deleteAll();
        candidatureRepository.deleteAll();
        offreRepository.deleteAll();
        userRepository.deleteAll();
        competenceRepository.deleteAll();
        // NB : on ne supprime pas les rôles — ils sont réutilisés
    }
}