package com.unz.eval.notification;

import com.unz.eval.dto.DTOs;
import com.unz.eval.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de création et gestion des notifications.
 */
@Service
public class NotificationService {

    private final NotificationRepository notifRepo;

    public NotificationService(NotificationRepository notifRepo) {
        this.notifRepo = notifRepo;
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
    }

    public void notifierBulletinPublie(User etudiant, String semestreLabel, Long bulletinId) {
        notifRepo.save(Notification.builder()
            .destinataire(etudiant)
            .type(Notification.Type.BULLETIN_PUBLIE)
            .titre("Bulletin publié")
            .message("Votre bulletin du semestre " + semestreLabel + " est disponible.")
            .referenceId(bulletinId)
            .build());
    }

    public void notifierNoteSaisie(User etudiant, String matiere, String valeur) {
        notifRepo.save(Notification.builder()
            .destinataire(etudiant)
            .type(Notification.Type.NOTE_SAISIE)
            .titre("Nouvelle note enregistrée")
            .message("Une note de " + valeur + "/20 a été saisie pour " + matiere + ".")
            .build());
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
