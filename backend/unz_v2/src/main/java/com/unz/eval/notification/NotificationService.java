package com.unz.eval.notification;

import com.unz.eval.dto.DTOs;
import com.unz.eval.entity.User;
import com.unz.eval.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de création et gestion des notifications.
 * v3.6 : en plus de la notification in-app, chaque événement déclenche
 * aussi un e-mail via EmailService (Brevo) — même logique défensive :
 * un souci d'envoi ne bloque jamais l'action déclenchante.
 */
@Service
public class NotificationService {

    private final NotificationRepository notifRepo;
    private final EmailService emailService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public NotificationService(NotificationRepository notifRepo, EmailService emailService) {
        this.notifRepo = notifRepo;
        this.emailService = emailService;
    }

    public void notifierQuizOuvert(List<User> etudiants, String quizTitre, Long quizId) {
        List<Notification> notifs = etudiants.stream().map(e ->
            Notification.builder()
                .destinataire(e)
                .type(Notification.Type.QUIZ_OUVERT)
                .titre("Nouveau quiz disponible")
                .message("Le quiz \"" + quizTitre + "\" est maintenant disponible.")
                .referenceId(quizId)
                .build()
        ).collect(Collectors.toList());
        notifRepo.saveAll(notifs);

        String lien = frontendUrl + "/quiz/" + quizId;
        etudiants.forEach(e -> emailService.envoyerQuizDisponible(e.getEmail(), e.getFirstName(), quizTitre, lien));
    }

    public void notifierNotesCloture(List<User> etudiants, String quizTitre, Long quizId) {
        List<Notification> notifs = etudiants.stream().map(e ->
            Notification.builder()
                .destinataire(e)
                .type(Notification.Type.NOTES_DISPONIBLES)
                .titre("Notes disponibles")
                .message("Vos résultats pour \"" + quizTitre + "\" sont maintenant visibles.")
                .referenceId(quizId)
                .build()
        ).collect(Collectors.toList());
        notifRepo.saveAll(notifs);

        String lien = frontendUrl + "/quiz/" + quizId + "/resultat";
        etudiants.forEach(e -> emailService.envoyerNotesDisponibles(e.getEmail(), e.getFirstName(), quizTitre, lien));
    }

    public void notifierBulletinPublie(User etudiant, String semestreLabel, Long bulletinId) {
        notifRepo.save(Notification.builder()
            .destinataire(etudiant)
            .type(Notification.Type.BULLETIN_PUBLIE)
            .titre("Bulletin publié")
            .message("Votre bulletin du semestre " + semestreLabel + " est disponible.")
            .referenceId(bulletinId)
            .build());

        String lien = frontendUrl + "/notes";
        emailService.envoyerBulletinDisponible(etudiant.getEmail(), etudiant.getFirstName(), semestreLabel, lien);
    }

    public void notifierNoteSaisie(User etudiant, String matiere, String valeur) {
        notifRepo.save(Notification.builder()
            .destinataire(etudiant)
            .type(Notification.Type.NOTE_SAISIE)
            .titre("Nouvelle note enregistrée")
            .message("Une note de " + valeur + "/20 a été saisie pour " + matiere + ".")
            .build());

        String lien = frontendUrl + "/notes";
        emailService.envoyerNoteSaisie(etudiant.getEmail(), etudiant.getFirstName(), matiere, valeur, lien);
    }

    public List<Notification> getMesNotifications(User user) {
        return notifRepo.findByDestinataireOrderByCreatedAtDesc(user);
    }

    public long getNbNonLues(User user) {
        return notifRepo.countByDestinataireAndLueFalse(user);
    }

    public void marquerLue(Long notifId, User user) {
        notifRepo.findById(notifId).ifPresent(n -> {
            if (n.getDestinataire().getId().equals(user.getId())) {
                n.setLue(true);
                notifRepo.save(n);
            }
        });
    }

    public void marquerToutesLues(User user) {
        List<Notification> nonLues = notifRepo
                .findByDestinataireAndLueFalseOrderByCreatedAtDesc(user);
        nonLues.forEach(n -> n.setLue(true));
        notifRepo.saveAll(nonLues);
    }
}

@RestController
@RequestMapping("/api/notifications")
class NotificationController {

    private final NotificationService notifService;

    public NotificationController(NotificationService notifService) {
        this.notifService = notifService;
    }

    /**
     * v3.2 : mappe désormais vers un DTO — la version précédente renvoyait
     * l'entité Notification brute, qui embarque l'utilisateur destinataire
     * complet (y compris le mot de passe haché) via la relation JPA. Faille corrigée.
     */
    @GetMapping
    public ResponseEntity<?> mes(@AuthenticationPrincipal User user) {
        List<DTOs.NotificationDTO> dto = notifService.getMesNotifications(user).stream()
                .map(n -> DTOs.NotificationDTO.builder()
                        .id(n.getId()).type(n.getType().name()).titre(n.getTitre())
                        .message(n.getMessage()).lue(n.isLue())
                        .createdAt(n.getCreatedAt()).referenceId(n.getReferenceId())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/count")
    public ResponseEntity<?> count(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(java.util.Map.of(
            "nonLues", notifService.getNbNonLues(user)
        ));
    }

    @PostMapping("/{id}/lire")
    public ResponseEntity<?> lire(@PathVariable Long id,
                                   @AuthenticationPrincipal User user) {
        notifService.marquerLue(id, user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/lire-tout")
    public ResponseEntity<?> lireTout(@AuthenticationPrincipal User user) {
        notifService.marquerToutesLues(user);
        return ResponseEntity.ok().build();
    }
}
