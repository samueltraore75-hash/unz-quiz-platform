package com.unz.eval.service;

import com.unz.eval.dto.DTOs;
import com.unz.eval.entity.*;
import com.unz.eval.exception.BadRequestException;
import com.unz.eval.exception.ResourceNotFoundException;
import com.unz.eval.notification.NotificationService;
import com.unz.eval.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Calcul des moyennes et bulletins.
 */
@Service
@Transactional(readOnly = true)
public class GradeService {

    private final NoteRepository noteRepo;
    private final BulletinRepository bulletinRepo;
    private final SemestreRepository semestreRepo;
    private final MatiereRepository matiereRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;

    public GradeService(NoteRepository noteRepo, BulletinRepository bulletinRepo, SemestreRepository semestreRepo, MatiereRepository matiereRepo, UserRepository userRepo, NotificationService notificationService) {
        this.noteRepo = noteRepo;
        this.bulletinRepo = bulletinRepo;
        this.semestreRepo = semestreRepo;
        this.matiereRepo = matiereRepo;
        this.userRepo = userRepo;
        this.notificationService = notificationService;
    }

    /** EF-19 : moyenne pondérée d'une matière */
    public BigDecimal moyenneMatiere(User etudiant, Matiere matiere) {
        List<Note> notes = noteRepo.findByEtudiantAndMatiere(etudiant, matiere);
        if (notes.isEmpty()) return null;
        BigDecimal totalPondere = BigDecimal.ZERO;
        BigDecimal totalPoids = BigDecimal.ZERO;
        for (Note n : notes) {
            totalPondere = totalPondere.add(n.getValeur().multiply(n.getPonderation()));
            totalPoids = totalPoids.add(n.getPonderation());
        }
        if (totalPoids.compareTo(BigDecimal.ZERO) == 0) return null;
        return totalPondere.divide(totalPoids, 2, RoundingMode.HALF_UP);
    }

    /** EF-20 : moyenne UE avec compensation entre matières */
    public BigDecimal moyenneUE(User etudiant, UE ue) {
        BigDecimal totalPondere = BigDecimal.ZERO;
        BigDecimal totalCoef = BigDecimal.ZERO;
        for (Matiere m : ue.getMatieres()) {
            BigDecimal moy = moyenneMatiere(etudiant, m);
            if (moy == null) continue;
            totalPondere = totalPondere.add(moy.multiply(m.getCoefficient()));
            totalCoef = totalCoef.add(m.getCoefficient());
        }
        if (totalCoef.compareTo(BigDecimal.ZERO) == 0) return null;
        return totalPondere.divide(totalCoef, 2, RoundingMode.HALF_UP);
    }

    /** EF-21 : moyenne semestre + crédits acquis */
    public BigDecimal moyenneSemestre(User etudiant, Semestre semestre) {
        BigDecimal totalPondere = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;
        for (UE ue : semestre.getUes()) {
            BigDecimal moy = moyenneUE(etudiant, ue);
            if (moy == null) continue;
            BigDecimal credits = BigDecimal.valueOf(ue.getCredits());
            totalPondere = totalPondere.add(moy.multiply(credits));
            totalCredits = totalCredits.add(credits);
        }
        if (totalCredits.compareTo(BigDecimal.ZERO) == 0) return null;
        return totalPondere.divide(totalCredits, 2, RoundingMode.HALF_UP);
    }

    public int[] creditsAcquis(User etudiant, Semestre semestre) {
        int total = semestre.getUes().stream().mapToInt(UE::getCredits).sum();
        int acquis = semestre.getUes().stream()
                .filter(ue -> {
                    BigDecimal moy = moyenneUE(etudiant, ue);
                    return moy != null && moy.compareTo(ue.getSeuilValidation()) >= 0;
                })
                .mapToInt(UE::getCredits).sum();
        return new int[]{acquis, total};
    }

    /** EF-22 / EF-23 : génération du bulletin */
    @Transactional
    public Bulletin genererBulletin(DTOs.GenerateBulletinRequest req, User admin) {
        // ENF-2 : seul l'admin peut générer
        if (!admin.isAdmin())
            throw new AccessDeniedException("Seul un Administrateur peut générer un bulletin.");

        User etudiant = userRepo.findById(req.getEtudiantId())
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant introuvable"));
        Semestre semestre = semestreRepo.findById(req.getSemestreId())
                .orElseThrow(() -> new ResourceNotFoundException("Semestre introuvable"));

        BigDecimal moy = moyenneSemestre(etudiant, semestre);
        int[] credits = creditsAcquis(etudiant, semestre);

        Bulletin bulletin = bulletinRepo.findByEtudiantAndSemestre(etudiant, semestre)
                .orElse(Bulletin.builder().etudiant(etudiant).semestre(semestre).build());

        bulletin.setMoyenneGenerale(moy);
        bulletin.setCreditsAcquis(credits[0]);
        bulletin.setCreditsTotal(credits[1]);
        bulletin.setSemestreValide(credits[0] == credits[1] && credits[1] > 0);
        bulletin.setGenerePar(admin);

        // EF-23 : publication explicite
        if (req.isPublier() && !bulletin.isPublie()) {
            bulletin.setPublie(true);
            bulletin.setDatePublication(LocalDateTime.now());
        }

        bulletin = bulletinRepo.save(bulletin);
        if (bulletin.isPublie()) {
            notificationService.notifierBulletinPublie(
                    etudiant, bulletin.getSemestre().toString(), bulletin.getId());
        }
        return bulletin;
    }

    /** EF-23 : acte de publication séparé */
    @Transactional
    public Bulletin publierBulletin(Long bulletinId, User admin) {
        if (!admin.isAdmin())
            throw new AccessDeniedException("Seul un Administrateur peut publier un bulletin.");
        Bulletin b = bulletinRepo.findById(bulletinId)
                .orElseThrow(() -> new ResourceNotFoundException("Bulletin introuvable"));
        if (b.isPublie()) throw new BadRequestException("Ce bulletin est déjà publié.");
        b.setPublie(true);
        b.setDatePublication(LocalDateTime.now());
        b.setGenerePar(admin);
        b = bulletinRepo.save(b);
        notificationService.notifierBulletinPublie(
                b.getEtudiant(), b.getSemestre().toString(), b.getId());
        return b;
    }

    public DTOs.BulletinDTO toBulletinDTO(Bulletin b) {
        List<DTOs.UeDetailDTO> detailUes = b.getSemestre().getUes().stream().map(ue -> {
            List<DTOs.MatiereDetailDTO> mats = ue.getMatieres().stream().map(m -> {
                BigDecimal moy = moyenneMatiere(b.getEtudiant(), m);
                return DTOs.MatiereDetailDTO.builder()
                        .matiere(m.getNom())
                        .coefficient(m.getCoefficient().doubleValue())
                        .moyenne(moy != null ? moy.doubleValue() : null)
                        .build();
            }).collect(Collectors.toList());
            BigDecimal moyUe = moyenneUE(b.getEtudiant(), ue);
            return DTOs.UeDetailDTO.builder()
                    .ue(ue.getNom()).credits(ue.getCredits())
                    .moyenneUe(moyUe != null ? moyUe.doubleValue() : null)
                    .matieres(mats).build();
        }).collect(Collectors.toList());

        return DTOs.BulletinDTO.builder()
                .id(b.getId())
                .etudiantNom(b.getEtudiant().getFullName())
                .semestreLabel(b.getSemestre().toString())
                .moyenneGenerale(b.getMoyenneGenerale())
                .creditsAcquis(b.getCreditsAcquis())
                .creditsTotal(b.getCreditsTotal())
                .semestreValide(b.isSemestreValide())
                .publie(b.isPublie())
                .datePublication(b.getDatePublication())
                .detailUes(detailUes)
                .build();
    }
}
