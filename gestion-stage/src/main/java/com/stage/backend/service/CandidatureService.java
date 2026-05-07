package com.stage.backend.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfWriter;
import com.stage.backend.dto.AcceptationRequest;
import com.stage.backend.dto.CandidatureRequest;
import com.stage.backend.dto.CandidatureResponse;
import com.stage.backend.dto.StatutRequest;
import com.stage.backend.entity.*;
import com.stage.backend.repository.CandidatureRepository;
import com.stage.backend.repository.InvitationRepository;
import com.stage.backend.repository.OffreRepository;
import com.stage.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CandidatureService {

    @Autowired private CandidatureRepository candidatureRepository;
    @Autowired private OffreRepository offreRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private InvitationRepository invitationRepository;

    @Value("${app.upload.dir:uploads/cv}")
    private String uploadDir;

    // ─── POSTULER ──────────────────────────────────────────────────────────────
    @Transactional
    public CandidatureResponse postuler(Long offreId, CandidatureRequest request,
                                        MultipartFile cvFile, String username) {
        User etudiant = getUser(username);

        boolean isEtudiant = etudiant.getRoles().stream()
                .anyMatch(r -> r.getName() == ERole.ROLE_ETUDIANT);
        if (!isEtudiant) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seul un étudiant peut postuler.");
        }
        if (candidatureRepository.existsByEtudiantIdAndOffreId(etudiant.getId(), offreId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vous avez déjà postulé à cette offre.");
        }
        Offre offre = offreRepository.findById(offreId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offre introuvable."));

        String cvFileName, cvFilePath;
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
            String uniqueName = UUID.randomUUID() + "_" + cvFile.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_");
            Path filePath = uploadPath.resolve(uniqueName);
            Files.copy(cvFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            cvFileName = cvFile.getOriginalFilename();
            cvFilePath = filePath.toString();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de l'upload du CV: " + e.getMessage());
        }

        Candidature c = new Candidature();
        c.setEtudiant(etudiant);
        c.setOffre(offre);
        c.setStatut(StatutCandidature.EN_ATTENTE);
        c.setLettreMotivation(request.getLettreMotivation());
        c.setDisponibilite(request.getDisponibilite());
        c.setTelephone(request.getTelephone());
        c.setNiveauEtudes(request.getNiveauEtudes());
        c.setEtablissement(request.getEtablissement());
        c.setLinkedinUrl(request.getLinkedinUrl());
        c.setPortfolioUrl(request.getPortfolioUrl());
        c.setCvFileName(cvFileName);
        c.setCvFilePath(cvFilePath);

        Candidature saved = candidatureRepository.save(c);
        return new CandidatureResponse(saved);
    }

    // ─── TÉLÉCHARGER LE CV ────────────────────────────────────────────────────
    public Path getCvPath(Long candidatureId, String username) {
        Candidature c = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidature introuvable."));
        if (!c.getOffre().getRecruteur().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé.");
        }
        Path cvPath = Paths.get(c.getCvFilePath()).toAbsolutePath().normalize();
        if (!Files.exists(cvPath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fichier CV introuvable.");
        }
        return cvPath;
    }

    // ─── MES CANDIDATURES ─────────────────────────────────────────────────────
    public List<CandidatureResponse> getMesCandidatures(String username) {
        User etudiant = getUser(username);
        return candidatureRepository.findByEtudiantId(etudiant.getId())
                .stream().map(CandidatureResponse::new).collect(Collectors.toList());
    }

    // ─── CANDIDATURES D'UNE OFFRE ─────────────────────────────────────────────
    public List<CandidatureResponse> getCandidaturesParOffre(Long offreId, String username) {
        Offre offre = offreRepository.findById(offreId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offre introuvable."));
        if (!offre.getRecruteur().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé.");
        }
        return candidatureRepository.findByOffreId(offreId)
                .stream().map(CandidatureResponse::new).collect(Collectors.toList());
    }

    // ─── CHANGER STATUT ───────────────────────────────────────────────────────
    @Transactional
    public CandidatureResponse changerStatut(Long candidatureId, StatutRequest request, String username) {
        Candidature c = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidature introuvable."));
        if (!c.getOffre().getRecruteur().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé.");
        }
        c.setStatut(request.getStatut());
        return new CandidatureResponse(candidatureRepository.save(c));
    }

    // ─── ANNULER ──────────────────────────────────────────────────────────────
    @Transactional
    public void annuler(Long candidatureId, String username) {
        Candidature c = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidature introuvable."));
        if (!c.getEtudiant().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé.");
        }
        try {
            if (c.getCvFilePath() != null) Files.deleteIfExists(Paths.get(c.getCvFilePath()).toAbsolutePath());
        } catch (IOException ignored) {}
        candidatureRepository.delete(c);
    }

    // ─── ACCEPTER CANDIDATURE AVEC FORMULAIRE DÉTAILLÉ ────────────────────────
    @Transactional
    public CandidatureResponse accepterCandidature(AcceptationRequest request, String recruteurUsername) {
        Candidature candidature = candidatureRepository.findById(request.getCandidatureId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidature introuvable"));
        if (!candidature.getOffre().getRecruteur().getUsername().equals(recruteurUsername)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'êtes pas autorisé à accepter cette candidature.");
        }
        candidature.setStatut(StatutCandidature.ACCEPTEE);
        candidatureRepository.save(candidature);

        if (invitationRepository.findByCandidatureId(candidature.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Une invitation a déjà été générée.");
        }

        Invitation invitation = Invitation.builder()
                .candidature(candidature)
                .societeAccueil(request.getSocieteAccueil())
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
                .mode(request.getMode())
                .remunere(request.isRemunere())
                .descriptionSujet(request.getDescriptionSujet())
                .conditionsComplementaires(request.getConditionsComplementaires())
                .build();
        invitationRepository.save(invitation);
        return new CandidatureResponse(candidature);
    }

    // ─── GÉNÉRER LE PDF D'INVITATION (VERSION PROFESSIONNELLE) ────────────────
    public byte[] genererPdfInvitation(Long candidatureId, String etudiantUsername) {
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidature non trouvée"));
        if (!candidature.getEtudiant().getUsername().equals(etudiantUsername)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès non autorisé.");
        }

        Invitation invitation = invitationRepository.findByCandidatureId(candidatureId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Aucune invitation trouvée pour cette candidature."));

        User etudiant = candidature.getEtudiant();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        document.setMargins(50, 50, 70, 50);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // ---- 1. En-tête (bandeau bleu) ----
            PdfPTable headerTable = new PdfPTable(1);
            headerTable.setWidthPercentage(100);
            PdfPCell headerCell = new PdfPCell();
            headerCell.setBackgroundColor(new Color(41, 128, 185));
            headerCell.setPadding(15);
            headerCell.setBorder(Rectangle.NO_BORDER);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.WHITE);
            headerCell.addElement(new Phrase("Lettre d'acceptation de stage", headerFont));
            headerTable.addCell(headerCell);
            document.add(headerTable);
            document.add(new Paragraph("\n"));

            // ---- 2. Société & date ----
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{2f, 1f});
            Font companyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(41, 128, 185));
            Phrase company = new Phrase(invitation.getSocieteAccueil() + "\n", companyFont);
            company.add(new Phrase("Service des ressources humaines", FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY)));
            PdfPCell leftCell = new PdfPCell(company);
            leftCell.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(leftCell);

            Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
            String dateStr = "Le " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            PdfPCell rightCell = new PdfPCell(new Phrase(dateStr, dateFont));
            rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            rightCell.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(rightCell);
            document.add(infoTable);
            document.add(new Paragraph("\n"));

            // ---- 3. Objet ----
            Font objetFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(41, 128, 185));
            Paragraph objet = new Paragraph("Objet : Acceptation de votre candidature de stage", objetFont);
            objet.setSpacingAfter(10);
            document.add(objet);

            // ---- 4. Corps de la lettre ----
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);
            Paragraph corps = new Paragraph(
                    String.format("%s,\n\n"
                                    + "Nous avons le plaisir de vous informer que votre candidature pour un stage au sein de notre société "
                                    + "a été retenue. Après examen de votre dossier, nous vous confirmons votre acceptation.\n\n"
                                    + "Voici les détails de votre stage :\n",
                            etudiant.getUsername()),
                    bodyFont);
            corps.setSpacingAfter(8);
            document.add(corps);

            // ---- 5. Tableau des détails ----
            PdfPTable detailsTable = new PdfPTable(2);
            detailsTable.setWidthPercentage(100);
            detailsTable.setWidths(new float[]{1f, 2f});
            detailsTable.setSpacingBefore(5);
            detailsTable.setSpacingAfter(15);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(41, 128, 185));
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);

            addDetailRow(detailsTable, "Période :",
                    invitation.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " au " +
                            invitation.getDateFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    labelFont, valueFont);
            addDetailRow(detailsTable, "Modalité :", invitation.getMode(), labelFont, valueFont);
            addDetailRow(detailsTable, "Rémunération :", invitation.isRemunere() ? "Oui (à préciser en entretien)" : "Non (stage non rémunéré)", labelFont, valueFont);
            addDetailRow(detailsTable, "Sujet du stage :", invitation.getDescriptionSujet(), labelFont, valueFont);
            if (invitation.getConditionsComplementaires() != null && !invitation.getConditionsComplementaires().isEmpty()) {
                addDetailRow(detailsTable, "Conditions :", invitation.getConditionsComplementaires(), labelFont, valueFont);
            }
            document.add(detailsTable);

            // ---- 6. Formule de politesse ----
            Paragraph politesse = new Paragraph(
                    "Nous vous souhaitons la bienvenue au sein de notre équipe et restons à votre disposition "
                            + "pour toute information complémentaire.\n\n"
                            + "Nous vous prions d'agréer, Madame, Monsieur, l'expression de nos salutations distinguées.\n\n",
                    bodyFont);
            politesse.setSpacingAfter(20);
            document.add(politesse);

            // ---- 7. Signature ----
            PdfPTable signatureTable = new PdfPTable(2);
            signatureTable.setWidthPercentage(100);
            signatureTable.setWidths(new float[]{1f, 1f});
            Font signFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
            PdfPCell signLeft = new PdfPCell(new Phrase("Signature du responsable\n\n(à compléter)", signFont));
            signLeft.setBorder(Rectangle.NO_BORDER);
            PdfPCell signRight = new PdfPCell(new Phrase("Cachet de l'entreprise", signFont));
            signRight.setHorizontalAlignment(Element.ALIGN_RIGHT);
            signRight.setBorder(Rectangle.NO_BORDER);
            signatureTable.addCell(signLeft);
            signatureTable.addCell(signRight);
            document.add(signatureTable);

            // ---- 8. Pied de page ----
            PdfPTable footerTable = new PdfPTable(1);
            footerTable.setWidthPercentage(100);
            PdfPCell footerCell = new PdfPCell(new Phrase(
                    invitation.getSocieteAccueil() + " - Tél : +33 1 23 45 67 89 - www.entreprise-exemple.com",
                    FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY)));
            footerCell.setBorder(Rectangle.TOP);
            footerCell.setPaddingTop(10);
            footerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            footerTable.addCell(footerCell);
            document.add(footerTable);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
        return out.toByteArray();
    }

    // ─── HELPER POUR LE TABLEAU DES DÉTAILS ───────────────────────────────────
    private void addDetailRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(4);
        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "-", valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(4);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    // ─── HELPER ───────────────────────────────────────────────────────────────
    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable."));
    }
}