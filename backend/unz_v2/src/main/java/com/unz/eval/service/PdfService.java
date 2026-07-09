package com.unz.eval.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.*;
import com.unz.eval.dto.DTOs;
import com.unz.eval.entity.Bulletin;
import com.unz.eval.exception.ResourceNotFoundException;
import com.unz.eval.repository.BulletinRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Génération de bulletins en PDF côté serveur avec iText 8.
 * EF-22 : le bulletin PDF est téléchargeable via GET /api/bulletins/{id}/pdf
 * ENF-2 : un étudiant ne peut télécharger que son propre bulletin publié.
 */
@Service
public class PdfService {

    private final BulletinRepository bulletinRepo;
    private final GradeService gradeService;

    public PdfService(BulletinRepository bulletinRepo, GradeService gradeService) {
        this.bulletinRepo = bulletinRepo;
        this.gradeService = gradeService;
    }

    private static final DeviceRgb COLOR_PRIMARY   = new DeviceRgb(27, 42, 74);
    private static final DeviceRgb COLOR_ACCENT    = new DeviceRgb(79, 110, 247);
    private static final DeviceRgb COLOR_SUCCESS   = new DeviceRgb(22, 163, 74);
    private static final DeviceRgb COLOR_DANGER    = new DeviceRgb(220, 38, 38);
    private static final DeviceRgb COLOR_BG_HEADER = new DeviceRgb(238, 241, 255);
    private static final DeviceRgb COLOR_TEXT_MUTED = new DeviceRgb(100, 116, 139);

    /**
     * ENF-2 : un étudiant ne télécharge que son propre bulletin publié.
     * v3.1 : restreint désormais aux étudiants (leur propre bulletin) et à l'Administrateur —
     * un enseignant n'a plus accès aux bulletins d'étudiants qui ne sont pas les siens
     * (faille corrigée : jusqu'ici n'importe quel enseignant pouvait télécharger le
     * bulletin de n'importe quel étudiant, sans lien avec ses classes).
     */
    public byte[] genererBulletinPdf(Long bulletinId, com.unz.eval.entity.User demandeur) {
        Bulletin bulletin = bulletinRepo.findById(bulletinId)
                .orElseThrow(() -> new ResourceNotFoundException("Bulletin introuvable"));

        if (demandeur.isEtudiant()) {
            if (!bulletin.getEtudiant().getId().equals(demandeur.getId()))
                throw new AccessDeniedException("Vous ne pouvez télécharger que votre propre bulletin.");
            if (!bulletin.isPublie())
                throw new AccessDeniedException("Ce bulletin n'est pas encore publié.");
        } else if (!demandeur.isAdmin()) {
            throw new AccessDeniedException("Seuls l'étudiant concerné et l'Administrateur peuvent télécharger ce bulletin.");
        }

        DTOs.BulletinDTO dto = gradeService.toBulletinDTO(bulletin);
        return buildPdf(dto, bulletin);
    }

    private byte[] buildPdf(DTOs.BulletinDTO dto, Bulletin bulletin) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document doc = new Document(pdf)) {

            doc.setMargins(40, 40, 40, 40);

            // ── En-tête ──────────────────────────────────────────────────
            Table header = new Table(UnitValue.createPercentArray(new float[]{1}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setBackgroundColor(COLOR_PRIMARY);

            Cell headerCell = new Cell()
                    .setBackgroundColor(COLOR_PRIMARY)
                    .setPadding(16)
                    .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);

            headerCell.add(new Paragraph("UNIVERSITÉ NORBERT ZONGO")
                    .setFontColor(ColorConstants.WHITE).setFontSize(9)
                    .setCharacterSpacing(1.5f));
            headerCell.add(new Paragraph("UFR Sciences et Techniques | Département Informatique")
                    .setFontColor(new DeviceRgb(148, 163, 184)).setFontSize(8));
            headerCell.add(new Paragraph("BULLETIN SEMESTRIEL")
                    .setFontColor(ColorConstants.WHITE).setFontSize(16).setBold()
                    .setMarginTop(6));

            header.addCell(headerCell);
            doc.add(header);
            doc.add(new Paragraph("\n"));

            // ── Infos étudiant ────────────────────────────────────────────
            doc.add(new Paragraph("Étudiant : " + dto.getEtudiantNom())
                    .setFontSize(11).setBold());
            doc.add(new Paragraph("Semestre : " + dto.getSemestreLabel())
                    .setFontSize(10).setFontColor(COLOR_TEXT_MUTED));
            if (bulletin.getDatePublication() != null) {
                doc.add(new Paragraph("Édité le : " +
                        bulletin.getDatePublication().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                        .setFontSize(10).setFontColor(COLOR_TEXT_MUTED));
            }
            doc.add(new Paragraph("\n"));

            // ── Détail par UE ─────────────────────────────────────────────
            if (dto.getDetailUes() != null) {
                for (DTOs.UeDetailDTO ue : dto.getDetailUes()) {
                    // En-tête UE
                    Table ueHeader = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                            .setWidth(UnitValue.createPercentValue(100))
                            .setMarginBottom(2);
                    Cell ueNameCell = new Cell().add(
                        new Paragraph(ue.getUe() + "  (" + ue.getCredits() + " crédits)")
                            .setFontSize(10).setBold().setFontColor(COLOR_PRIMARY))
                        .setBackgroundColor(COLOR_BG_HEADER).setPadding(6)
                        .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
                    Cell ueMoyCell = new Cell().add(
                        new Paragraph(ue.getMoyenneUe() != null
                            ? String.format("%.2f/20", ue.getMoyenneUe()) : "—")
                            .setFontSize(10).setBold().setFontColor(COLOR_ACCENT)
                            .setTextAlignment(TextAlignment.RIGHT))
                        .setBackgroundColor(COLOR_BG_HEADER).setPadding(6)
                        .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
                    ueHeader.addCell(ueNameCell).addCell(ueMoyCell);
                    doc.add(ueHeader);

                    // Matières
                    if (ue.getMatieres() != null) {
                        for (DTOs.MatiereDetailDTO mat : ue.getMatieres()) {
                            Table matRow = new Table(UnitValue.createPercentArray(new float[]{3, 1, 1}))
                                    .setWidth(UnitValue.createPercentValue(100))
                                    .setMarginBottom(1);
                            matRow.addCell(new Cell().add(new Paragraph("   " + mat.getMatiere())
                                .setFontSize(9).setFontColor(COLOR_TEXT_MUTED))
                                .setPadding(4).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
                            matRow.addCell(new Cell().add(new Paragraph("coef. " + mat.getCoefficient())
                                .setFontSize(8).setFontColor(COLOR_TEXT_MUTED)
                                .setTextAlignment(TextAlignment.CENTER))
                                .setPadding(4).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
                            matRow.addCell(new Cell().add(new Paragraph(
                                mat.getMoyenne() != null ? String.format("%.2f", mat.getMoyenne()) : "—")
                                .setFontSize(9).setBold().setTextAlignment(TextAlignment.RIGHT))
                                .setPadding(4).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
                            doc.add(matRow);
                        }
                    }
                    doc.add(new Paragraph("\n").setFontSize(4));
                }
            }

            // ── Récapitulatif ─────────────────────────────────────────────
            doc.add(new com.itextpdf.layout.element.LineSeparator(
                    new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(0.5f))
                    .setMarginBottom(8));

            Table recap = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                    .setWidth(UnitValue.createPercentValue(100));

            addRecapCell(recap, dto.getMoyenneGenerale() != null
                ? String.format("%.2f/20", dto.getMoyenneGenerale().doubleValue()) : "—",
                "Moyenne générale", COLOR_ACCENT);
            addRecapCell(recap, dto.getCreditsAcquis() + "/" + dto.getCreditsTotal(),
                "Crédits acquis", COLOR_PRIMARY);
            addRecapCell(recap, dto.isSemestreValide() ? "Validé ✓" : "Non validé",
                "Statut", dto.isSemestreValide() ? COLOR_SUCCESS : COLOR_DANGER);

            doc.add(recap);
            doc.add(new Paragraph("\n"));

            // ── Pied de page ──────────────────────────────────────────────
            doc.add(new Paragraph(
                "Document officiel généré par la plateforme UNZ Évaluation — " +
                java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setFontSize(8).setFontColor(COLOR_TEXT_MUTED)
                .setTextAlignment(TextAlignment.CENTER));

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }

        return baos.toByteArray();
    }

    private void addRecapCell(Table table, String value, String label, DeviceRgb color) {
        table.addCell(new Cell()
            .add(new Paragraph(value).setFontSize(14).setBold().setFontColor(color)
                .setTextAlignment(TextAlignment.CENTER))
            .add(new Paragraph(label).setFontSize(9).setFontColor(COLOR_TEXT_MUTED)
                .setTextAlignment(TextAlignment.CENTER))
            .setPadding(10).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
            .setBackgroundColor(new DeviceRgb(241, 245, 249)));
    }
}
