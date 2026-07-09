package com.unz.eval.controller;

import com.unz.eval.dto.DTOs;
import com.unz.eval.entity.*;
import com.unz.eval.exception.*;
import com.unz.eval.repository.*;
import com.unz.eval.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import java.util.List;
import java.util.stream.Collectors;

// ══════════════════════════════════════════════════════════════════════════
// USER CONTROLLER (Admin uniquement — ENF-2)
// AUTH (login/refresh/logout/me) géré entièrement par AuthControllerV2
// ══════════════════════════════════════════════════════════════════════════
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
class UserController {

    private final AuthService authService;
    private final UserRepository userRepo;
    private final ClasseRepository classeRepo;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final com.unz.eval.service.EmailService emailService;
    private final InscriptionRepository inscriptionRepo;
    private final NoteRepository noteRepo;
    private final TentativeRepository tentativeRepo;
    private final AffectationEnseignantRepository affectationRepo;
    private final BulletinRepository bulletinRepo;
    private final DeliberationRepository deliberationRepo;
    private final ImportLogRepository importLogRepo;
    private final QuizRepository quizRepo;
    private final RefreshTokenRepository refreshTokenRepo;
    private final PasswordResetTokenRepository passwordResetTokenRepo;
    private final com.unz.eval.notification.NotificationRepository notificationRepo;

    public UserController(AuthService authService, UserRepository userRepo, ClasseRepository classeRepo,
                           org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
                           com.unz.eval.service.EmailService emailService,
                           InscriptionRepository inscriptionRepo, NoteRepository noteRepo,
                           TentativeRepository tentativeRepo, AffectationEnseignantRepository affectationRepo,
                           BulletinRepository bulletinRepo, DeliberationRepository deliberationRepo,
                           ImportLogRepository importLogRepo, QuizRepository quizRepo,
                           RefreshTokenRepository refreshTokenRepo, PasswordResetTokenRepository passwordResetTokenRepo,
                           com.unz.eval.notification.NotificationRepository notificationRepo) {
        this.authService = authService;
        this.userRepo = userRepo;
        this.classeRepo = classeRepo;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.inscriptionRepo = inscriptionRepo;
        this.noteRepo = noteRepo;
        this.tentativeRepo = tentativeRepo;
        this.affectationRepo = affectationRepo;
        this.bulletinRepo = bulletinRepo;
        this.deliberationRepo = deliberationRepo;
        this.importLogRepo = importLogRepo;
        this.quizRepo = quizRepo;
        this.refreshTokenRepo = refreshTokenRepo;
        this.passwordResetTokenRepo = passwordResetTokenRepo;
        this.notificationRepo = notificationRepo;
    }

    @GetMapping
    public ResponseEntity<List<DTOs.UserDTO>> listUsers(
            @RequestParam(required = false) String role) {
        List<User> users = role != null
                ? userRepo.findByRole(User.Role.valueOf(role.toUpperCase()))
                : userRepo.findAll();
        return ResponseEntity.ok(users.stream().map(authService::toUserDTO).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<DTOs.UserDTO> createUser(@Valid @RequestBody DTOs.CreateUserRequest req) {
        User user = authService.createUser(req, classeRepo);
        return ResponseEntity.status(201).body(authService.toUserDTO(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        user.setActive(false);
        userRepo.save(user);
        return ResponseEntity.ok().build();
    }

    /**
     * Suppression DÉFINITIVE d'un utilisateur (distincte de la désactivation ci-dessus).
     * Contrairement à deactivateUser (soft delete réversible), cette action retire la
     * ligne de la table `users`. Pour préserver l'intégrité de l'historique académique
     * (notes, tentatives, bulletins, etc.), la suppression est refusée dès que
     * l'utilisateur possède des données associées : dans ce cas l'admin doit se
     * contenter de désactiver le compte.
     */
    @DeleteMapping("/{id}/definitif")
    @com.unz.eval.audit.Audited(action = "DELETE_USER", description = "Suppression définitive d'un utilisateur")
    public ResponseEntity<?> supprimerDefinitivement(@PathVariable Long id,
                                                      @AuthenticationPrincipal User admin) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        if (admin != null && admin.getId().equals(user.getId())) {
            throw new BadRequestException("Vous ne pouvez pas supprimer votre propre compte.");
        }

        List<String> blocages = new java.util.ArrayList<>();
        if (inscriptionRepo.existsByEtudiant(user)) blocages.add("inscriptions");
        if (noteRepo.existsByEtudiant(user) || noteRepo.existsBySaisieParr(user)) blocages.add("notes");
        if (tentativeRepo.existsByEtudiant(user)) blocages.add("tentatives de quiz");
        if (affectationRepo.existsByEnseignant(user)) blocages.add("affectations d'enseignement");
        if (bulletinRepo.existsByEtudiant(user) || bulletinRepo.existsByGenerePar(user)) blocages.add("bulletins");
        if (deliberationRepo.existsByValidePar(user)) blocages.add("délibérations");
        if (importLogRepo.existsByImportePar(user)) blocages.add("imports de questions");
        if (quizRepo.existsByCreePar(user)) blocages.add("quiz créés");

        if (!blocages.isEmpty()) {
            throw new BadRequestException(
                    "Impossible de supprimer définitivement ce compte : des données lui sont associées ("
                            + String.join(", ", blocages)
                            + "). Désactivez-le plutôt afin de préserver l'historique académique.");
        }

        refreshTokenRepo.deleteAll(refreshTokenRepo.findAllByUser(user));
        passwordResetTokenRepo.deleteAll(passwordResetTokenRepo.findByUser(user));
        notificationRepo.deleteAll(notificationRepo.findByDestinataireOrderByCreatedAtDesc(user));

        String username = user.getUsername();
        userRepo.delete(user);

        return ResponseEntity.ok(Map.of("message", "Le compte de " + username + " a été supprimé définitivement."));
    }

    /**
     * v3.2 : réinitialisation de mot de passe par l'Administrateur.
     * Un mot de passe temporaire est généré ici et envoyé par e-mail à
     * l'utilisateur (voir EmailService). Reste retourné dans la réponse
     * aussi, en secours si l'envoi d'e-mail échoue.
     */
    @PostMapping("/{id}/reinitialiser-mot-de-passe")
    public ResponseEntity<?> reinitialiserMotDePasse(@PathVariable Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        String motDePasseTemporaire = authService.genererMotDePasseTemporaire();
        user.setPassword(passwordEncoder.encode(motDePasseTemporaire));
        userRepo.save(user);
        emailService.envoyerMotDePasseTemporaire(user.getEmail(), user.getFirstName(), user.getUsername(), motDePasseTemporaire);
        return ResponseEntity.ok(java.util.Map.of(
                "username", user.getUsername(),
                "motDePasseTemporaire", motDePasseTemporaire));
    }

    /** v3.3 : liste des demandes de compte en attente de validation */
    @GetMapping("/en-attente")
    public ResponseEntity<List<DTOs.DemandeCompteDTO>> enAttente() {
        return ResponseEntity.ok(
            userRepo.findByStatutCompteOrderByIdDesc(User.StatutCompte.EN_ATTENTE)
                .stream().map(this::toDemandeDTO).toList()
        );
    }

    /** v3.3 : valider une demande → statut ACTIF */
    @PostMapping("/{id}/valider")
    public ResponseEntity<?> validerCompte(@PathVariable Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        if (user.getStatutCompte() != User.StatutCompte.EN_ATTENTE) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "Ce compte n'est pas en attente de validation."));
        }
        user.setStatutCompte(User.StatutCompte.ACTIF);
        userRepo.save(user);
        emailService.envoyerBienvenue(user.getEmail(), user.getFirstName(), user.getUsername());
        return ResponseEntity.ok(java.util.Map.of("message", "Compte de " + user.getUsername() + " activé avec succès."));
    }

    /** v3.3 : rejeter une demande → statut REJETE */
    @PostMapping("/{id}/rejeter")
    public ResponseEntity<?> rejeterCompte(@PathVariable Long id,
                                           @RequestBody(required = false) java.util.Map<String,String> body) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        user.setStatutCompte(User.StatutCompte.REJETE);
        userRepo.save(user);
        return ResponseEntity.ok(java.util.Map.of("message", "Demande de " + user.getUsername() + " rejetée."));
    }

    private DTOs.DemandeCompteDTO toDemandeDTO(User u) {
        DTOs.DemandeCompteDTO dto = new DTOs.DemandeCompteDTO();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setFirstName(u.getFirstName());
        dto.setLastName(u.getLastName());
        dto.setEmail(u.getEmail());
        dto.setRole(u.getRole().name());
        dto.setStatutCompte(u.getStatutCompte().name());
        return dto;
    }
}

// ══════════════════════════════════════════════════════════════════════════
// QUIZ CONTROLLER
// ══════════════════════════════════════════════════════════════════════════
@RestController
@RequestMapping("/api/quizzes")
class QuizController {

    private final QuizService quizService;
    private final TentativeRepository tentativeRepo;
    private final StatsService statsService;

    public QuizController(QuizService quizService, TentativeRepository tentativeRepo, StatsService statsService) {
        this.quizService = quizService;
        this.tentativeRepo = tentativeRepo;
        this.statsService = statsService;
    }

    @GetMapping
    public ResponseEntity<List<DTOs.QuizListDTO>> list(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(quizService.getQuizzesForUser(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DTOs.QuizDetailDTO> detail(@PathVariable Long id,
                                                      @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(quizService.getQuizDetail(id, user));
    }

    @PostMapping
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public ResponseEntity<DTOs.QuizListDTO> create(@Valid @RequestBody DTOs.CreateQuizRequest req,
                                                    @AuthenticationPrincipal User user) {
        Quiz quiz = quizService.createQuiz(req, user);
        return ResponseEntity.status(201).body(quizService.getQuizzesForUser(user)
                .stream().filter(q -> q.getId().equals(quiz.getId())).findFirst()
                .orElseThrow());
    }

    @PostMapping("/{id}/questions")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public ResponseEntity<Void> addQuestions(@PathVariable Long id,
                                              @Valid @RequestBody DTOs.AddQuestionsRequest req,
                                              @AuthenticationPrincipal User user) {
        quizService.addQuestions(id, req.getQuestionIds(), user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/cloturer")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public ResponseEntity<Void> cloturer(@PathVariable Long id,
                                          @AuthenticationPrincipal User user) {
        quizService.cloturerQuiz(id, user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id,
                                           @AuthenticationPrincipal User user) {
        quizService.supprimerQuiz(id, user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/stats")
    @PreAuthorize("hasAnyRole('ENSEIGNANT','ADMIN')")
    public ResponseEntity<DTOs.QuizStatsDTO> stats(@PathVariable Long id,
                                                    @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(statsService.statistiquesQuiz(id, user));
    }

    @GetMapping("/{id}/notes")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public ResponseEntity<?> notesClasse(@PathVariable Long id,
                                          @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(statsService.notesClasse(id, user));
    }
}

// ══════════════════════════════════════════════════════════════════════════
// TENTATIVE CONTROLLER
// ══════════════════════════════════════════════════════════════════════════
@RestController
@RequestMapping("/api")
class TentativeController {

    private final QuizService quizService;
    private final TentativeRepository tentativeRepo;

    public TentativeController(QuizService quizService, TentativeRepository tentativeRepo) {
        this.quizService = quizService;
        this.tentativeRepo = tentativeRepo;
    }

    @PostMapping("/quizzes/{quizId}/start")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<?> start(@PathVariable Long quizId,
                                    @AuthenticationPrincipal User user) {
        Tentative t = quizService.demarrerTentative(quizId, user);
        return ResponseEntity.status(201).body(java.util.Map.of(
                "tentativeId", t.getId(),
                "dateDebut", t.getDateDebut()));
    }

    /** v3.2 : reprise d'une tentative en cours (heure de début réelle + réponses déjà sauvegardées) */
    @GetMapping("/tentatives/{id}")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<DTOs.TentativeEnCoursDTO> enCours(@PathVariable Long id,
                                                             @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(quizService.getTentativeEnCours(id, user));
    }

    /** v3.2 : sauvegarde immédiate d'une réponse — appelée à chaque interaction, pas seulement à la fin */
    @PostMapping("/tentatives/{id}/reponses")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<Void> sauvegarderReponse(@PathVariable Long id,
                                                    @Valid @RequestBody DTOs.SubmitReponseRequest req,
                                                    @AuthenticationPrincipal User user) {
        quizService.sauvegarderReponse(id, req, user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tentatives/{id}/submit")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<?> submit(@PathVariable Long id, @AuthenticationPrincipal User user) {
        Tentative t = quizService.soumettreTentative(id, user);
        return ResponseEntity.ok(toResultDTO(t));
    }

    @GetMapping("/tentatives/mine")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<List<DTOs.TentativeResultDTO>> mine(@AuthenticationPrincipal User user) {
        List<DTOs.TentativeResultDTO> results = tentativeRepo
                .findByEtudiantOrderByDateDebutDesc(user)
                .stream().map(this::toResultDTO).collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }

    /** v3.2 : progression regroupée par matière, en un seul appel (EF-11) */
    @GetMapping("/progression")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<List<DTOs.ProgressionMatiereDTO>> progression(@AuthenticationPrincipal User user) {
        List<Tentative> toutes = tentativeRepo.findByEtudiantOrderByDateDebutDesc(user)
                .stream().filter(t -> t.getDateSoumission() != null).collect(Collectors.toList());

        Map<String, List<Tentative>> parMatiere = toutes.stream()
                .collect(Collectors.groupingBy(t -> t.getQuiz().getMatiere().getNom(), LinkedHashMap::new, Collectors.toList()));

        List<DTOs.ProgressionMatiereDTO> resultat = parMatiere.entrySet().stream().map(entry -> {
            List<DTOs.ProgressionItemDTO> historique = entry.getValue().stream()
                    .sorted(Comparator.comparing(Tentative::getDateSoumission))
                    .map(t -> {
                        Double n20 = null;
                        if (t.getQuiz().isNotesVisibles() && t.getNoteObtenue() != null) {
                            double bareme = t.getQuiz().getBaremeTotal();
                            if (bareme > 0) n20 = Math.round(t.getNoteObtenue().doubleValue() / bareme * 20 * 100.0) / 100.0;
                        }
                        return DTOs.ProgressionItemDTO.builder()
                                .quizId(t.getQuiz().getId())
                                .quizTitre(t.getQuiz().getTitre())
                                .typeQuiz(t.getQuiz().getTypeQuiz().name())
                                .date(t.getDateSoumission())
                                .noteSur20(n20)
                                .build();
                    }).collect(Collectors.toList());
            return DTOs.ProgressionMatiereDTO.builder()
                    .matiere(entry.getKey())
                    .etudiant(user.getFullName())
                    .historique(historique).build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(resultat);
    }

    private DTOs.TentativeResultDTO toResultDTO(Tentative t) {
        boolean visible = t.getQuiz().isNotesVisibles();
        Double n20 = null;
        Double nBareme = null;
        if (visible && t.getNoteObtenue() != null) {
            double b = t.getQuiz().getBaremeTotal();
            if (b > 0) {
                n20 = Math.round(t.getNoteObtenue().doubleValue() / b * 20 * 100.0) / 100.0;
                nBareme = Math.round(t.getNoteObtenueSurNoteSur() * 100.0) / 100.0;
            }
        }
        List<DTOs.QuestionCorrigeeDTO> reponses = null;
        if (visible && t.getReponses() != null) {
            reponses = t.getReponses().stream().map(r -> {
                Question question = r.getQuestion();
                List<DTOs.ChoixCorrigeDTO> choix = question.getChoix() == null ? List.of() : question.getChoix().stream()
                        .map(c -> DTOs.ChoixCorrigeDTO.builder()
                                .id(c.getId()).texte(c.getTexte()).estCorrect(c.isEstCorrect()).build())
                        .collect(Collectors.toList());
                return DTOs.QuestionCorrigeeDTO.builder()
                        .id(question.getId())
                        .enonce(question.getEnonce())
                        .type(question.getType().name())
                        .explication(question.getExplication())
                        .choix(choix)
                        .reponseTexteEtudiant(r.getReponseTexte())
                        .estCorrecte(r.estCorrecte())
                        .correctionManuelle(question.isCorrectionManuelle())
                        .build();
            }).collect(Collectors.toList());
        }
        return DTOs.TentativeResultDTO.builder()
                .id(t.getId())
                .quizTitre(t.getQuiz().getTitre())
                .matiere(t.getQuiz().getMatiere().getNom())
                .dateDebut(t.getDateDebut())
                .dateSoumission(t.getDateSoumission())
                .noteObtenue(visible ? t.getNoteObtenue() : null)
                .baremeTotal(visible ? t.getQuiz().getBaremeTotal() : null)
                .noteSur20(n20)
                .noteSurBareme(nBareme)
                .bareme(visible ? t.getQuiz().getNoteSur() : null)
                .reponses(reponses)
                .build();
    }
}

// ══════════════════════════════════════════════════════════════════════════
// NOTES & BULLETINS CONTROLLER
// ══════════════════════════════════════════════════════════════════════════
@RestController
@RequestMapping("/api")
class GradeController {

    private final GradeService gradeService;
    private final NoteRepository noteRepo;
    private final MatiereRepository matiereRepo;
    private final UserRepository userRepo;
    private final BulletinRepository bulletinRepo;
    private final com.unz.eval.notification.NotificationService notificationService;

    public GradeController(GradeService gradeService, NoteRepository noteRepo, MatiereRepository matiereRepo, UserRepository userRepo, BulletinRepository bulletinRepo, com.unz.eval.notification.NotificationService notificationService) {
        this.gradeService = gradeService;
        this.noteRepo = noteRepo;
        this.matiereRepo = matiereRepo;
        this.userRepo = userRepo;
        this.bulletinRepo = bulletinRepo;
        this.notificationService = notificationService;
    }

    @PostMapping("/notes/matieres/{matiereId}")
    @PreAuthorize("hasAnyRole('ENSEIGNANT','ADMIN')")
    public ResponseEntity<?> saisirNote(@PathVariable Long matiereId,
                                         @Valid @RequestBody DTOs.CreateNoteRequest req,
                                         @AuthenticationPrincipal User user) {
        Matiere matiere = matiereRepo.findById(matiereId)
                .orElseThrow(() -> new ResourceNotFoundException("Matière introuvable"));
        if (user.isEnseignant() && !matiere.estEnseignePar(user))
            throw new AccessDeniedException("Vous ne pouvez saisir des notes que pour vos matières.");

        User etudiant = userRepo.findById(req.getEtudiantId())
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant introuvable"));
        Note note = Note.builder()
                .etudiant(etudiant).matiere(matiere)
                .typeNote(Note.TypeNote.valueOf(req.getTypeNote()))
                .valeur(req.getValeur()).ponderation(req.getPonderation())
                .saisieParr(user).dateSaisie(java.time.LocalDateTime.now()).build();
        noteRepo.save(note);
        notificationService.notifierNoteSaisie(etudiant, matiere.getNom(), note.getValeur().toString());
        return ResponseEntity.status(201).body(java.util.Map.of("id", note.getId(), "valeur", note.getValeur()));
    }

    @PostMapping("/bulletins/generer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DTOs.BulletinDTO> generer(@Valid @RequestBody DTOs.GenerateBulletinRequest req,
                                                     @AuthenticationPrincipal User admin) {
        Bulletin b = gradeService.genererBulletin(req, admin);
        return ResponseEntity.status(201).body(gradeService.toBulletinDTO(b));
    }

    @PostMapping("/bulletins/{id}/publier")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DTOs.BulletinDTO> publier(@PathVariable Long id,
                                                     @AuthenticationPrincipal User admin) {
        Bulletin b = gradeService.publierBulletin(id, admin);
        return ResponseEntity.ok(gradeService.toBulletinDTO(b));
    }

    @GetMapping("/bulletins/mine")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<List<DTOs.BulletinDTO>> mine(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bulletinRepo.findByEtudiantAndPublieTrue(user)
                .stream().map(gradeService::toBulletinDTO).collect(Collectors.toList()));
    }

    @GetMapping("/bulletins/etudiant/{etudiantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DTOs.BulletinDTO>> parEtudiant(@PathVariable Long etudiantId) {
        User etudiant = userRepo.findById(etudiantId)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant introuvable"));
        return ResponseEntity.ok(bulletinRepo.findByEtudiant(etudiant)
                .stream().map(gradeService::toBulletinDTO).collect(Collectors.toList()));
    }
}