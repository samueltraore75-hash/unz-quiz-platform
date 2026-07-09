package com.unz.eval.controller;

import com.opencsv.CSVReader;
import com.unz.eval.audit.Audited;
import com.unz.eval.dto.DTOs;
import com.unz.eval.entity.*;
import com.unz.eval.exception.*;
import com.unz.eval.notification.NotificationService;
import com.unz.eval.notification.NotificationRepository;
import com.unz.eval.repository.*;
import com.unz.eval.security.*;
import com.unz.eval.service.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

// ══════════════════════════════════════════════════════════════════════════
// AUTH CONTROLLER v2 — avec Refresh Token
// ══════════════════════════════════════════════════════════════════════════

@RestController
@RequestMapping("/api/auth")
class AuthControllerV2 {

    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenRepository refreshTokenRepo;
    private final UserRepository userRepo;
    private final AuthService authService;

    public AuthControllerV2(AuthenticationManager authManager, JwtUtils jwtUtils, RefreshTokenRepository refreshTokenRepo, UserRepository userRepo, AuthService authService) {
        this.authManager = authManager;
        this.jwtUtils = jwtUtils;
        this.refreshTokenRepo = refreshTokenRepo;
        this.userRepo = userRepo;
        this.authService = authService;
    }
    @org.springframework.beans.factory.annotation.Value("${app.jwt.refresh-expiration-ms}")
    private Long refreshExpMs;

    /** EF-1 + Audit : connexion sécurisée avec rotation des tokens */
    @PostMapping("/login")
    @Audited(action = "LOGIN", description = "Tentative de connexion utilisateur")
    public ResponseEntity<DTOs.LoginResponse> login(@Valid @RequestBody DTOs.LoginRequest req) {
        authManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));

        User user = userRepo.findByUsername(req.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        // Révoquer TOUS les anciens refresh tokens (pas seulement le premier)
        refreshTokenRepo.findAllByUser(user).forEach(t -> {
            t.setRevoked(true);
            refreshTokenRepo.save(t);
        });

        // Créer un nouveau refresh token
        RefreshToken refreshToken = RefreshToken.builder()
            .token(UUID.randomUUID().toString())
            .user(user)
            .expiresAt(LocalDateTime.now().plusNanos(refreshExpMs * 1_000_000))
            .build();
        refreshTokenRepo.save(refreshToken);

        return ResponseEntity.ok(DTOs.LoginResponse.builder()
            .accessToken(jwtUtils.generateAccessToken(user))
            .refreshToken(refreshToken.getToken())
            .role(user.getRole().name())
            .user(authService.toUserDTO(user))
            .build());
    }

    /**
     * ENF-7 : renouvellement du token avec rotation.
     * L'ancien refresh token est invalidé, un nouveau est créé.
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        String tokenStr = body.get("refreshToken");
        if (tokenStr == null) throw new BadRequestException("refreshToken requis.");

        RefreshToken token = refreshTokenRepo.findByToken(tokenStr)
            .orElseThrow(() -> new BadRequestException("Refresh token invalide."));

        if (!token.isValid()) throw new BadRequestException("Refresh token expiré ou révoqué.");

        // Rotation : invalider l'ancien, créer le nouveau
        token.setRevoked(true);
        refreshTokenRepo.save(token);

        RefreshToken newToken = RefreshToken.builder()
            .token(UUID.randomUUID().toString())
            .user(token.getUser())
            .expiresAt(LocalDateTime.now().plusNanos(refreshExpMs * 1_000_000))
            .build();
        refreshTokenRepo.save(newToken);

        return ResponseEntity.ok(Map.of(
            "accessToken", jwtUtils.generateAccessToken(token.getUser()),
            "refreshToken", newToken.getToken()
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<DTOs.UserDTO> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(authService.toUserDTO(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody(required = false) Map<String, String> body,
                                     @AuthenticationPrincipal User user) {
        if (body != null && body.containsKey("refreshToken")) {
            refreshTokenRepo.findByToken(body.get("refreshToken")).ifPresent(t -> {
                t.setRevoked(true);
                refreshTokenRepo.save(t);
            });
        }
        return ResponseEntity.ok(Map.of("message", "Déconnexion réussie."));
    }

    /**
     * Mot de passe oublié (self-service) : l'utilisateur renseigne son e-mail,
     * reçoit un lien de réinitialisation par e-mail (valable 30 minutes).
     * Réponse volontairement identique que l'e-mail existe ou non (on ne
     * révèle jamais si une adresse est enregistrée dans le système).
     */
    @PostMapping("/mot-de-passe-oublie")
    public ResponseEntity<?> motDePasseOublie(@Valid @RequestBody DTOs.ForgotPasswordRequest req) {
        authService.motDePasseOublie(req.getEmail());
        return ResponseEntity.ok(Map.of("message",
                "Si cette adresse est associée à un compte, un lien de réinitialisation vient de lui être envoyé."));
    }

    /**
     * Confirmation de la réinitialisation : appelée depuis la page où l'utilisateur
     * atterrit en cliquant sur le lien reçu par e-mail (?token=...).
     */
    @PostMapping("/reinitialiser-mot-de-passe")
    public ResponseEntity<?> reinitialiserMotDePasse(@Valid @RequestBody DTOs.ResetPasswordRequest req) {
        authService.reinitialiserMotDePasse(req.getToken(), req.getNouveauMotDePasse());
        return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé avec succès. Vous pouvez vous connecter."));
    }

    /** Changement de mot de passe par l'utilisateur connecté, depuis son profil. */
    @PostMapping("/changer-mot-de-passe")
    public ResponseEntity<?> changerMotDePasse(@Valid @RequestBody DTOs.ChangePasswordRequest req,
                                                @AuthenticationPrincipal User user) {
        authService.changerMotDePasse(user, req.getAncienMotDePasse(), req.getNouveauMotDePasse());
        return ResponseEntity.ok(Map.of("message", "Mot de passe modifié avec succès."));
    }
}

// ══════════════════════════════════════════════════════════════════════════
// INSCRIPTION LIBRE — création de compte soumise à validation Admin
// ══════════════════════════════════════════════════════════════════════════
@RestController
@RequestMapping("/api/auth")
class InscriptionController {

    private final UserRepository userRepo;
    private final ClasseRepository classeRepo;
    private final NotificationRepository notifRepo;
    private final PasswordEncoder passwordEncoder;
    private final AnneeAcademiqueRepository anneeRepo;
    private final InscriptionRepository inscriptionRepo;

    public InscriptionController(UserRepository userRepo, ClasseRepository classeRepo, NotificationRepository notifRepo, PasswordEncoder passwordEncoder, AnneeAcademiqueRepository anneeRepo, InscriptionRepository inscriptionRepo) {
        this.userRepo = userRepo;
        this.classeRepo = classeRepo;
        this.notifRepo = notifRepo;
        this.passwordEncoder = passwordEncoder;
        this.anneeRepo = anneeRepo;
        this.inscriptionRepo = inscriptionRepo;
    }

    @PostMapping("/inscription")
    @Transactional
    public ResponseEntity<?> inscrire(@RequestBody @Validated DTOs.InscriptionLibreRequest req) {

        // Seuls ETUDIANT et ENSEIGNANT peuvent s'auto-inscrire
        User.Role role;
        try {
            role = User.Role.valueOf(req.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Rôle invalide. Choisissez ETUDIANT ou ENSEIGNANT."));
        }
        if (role == User.Role.ADMIN) {
            return ResponseEntity.badRequest().body(Map.of("message", "La création d'un compte administrateur n'est pas autorisée via ce formulaire."));
        }

        // Vérifier unicité du username
        if (userRepo.existsByUsername(req.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cet identifiant est déjà utilisé. Choisissez-en un autre."));
        }

        // Créer le compte avec statut EN_ATTENTE
        User user = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail())
                .role(role)
                .active(true)
                .statutCompte(User.StatutCompte.EN_ATTENTE)
                .build();
        userRepo.save(user);

        // Notifier tous les admins
        userRepo.findByRole(User.Role.ADMIN).forEach(admin -> {
            com.unz.eval.notification.Notification notif = com.unz.eval.notification.Notification.builder()
                    .destinataire(admin)
                    .type(com.unz.eval.notification.Notification.Type.DEMANDE_COMPTE)
                    .titre("Nouvelle demande de compte")
                    .message(req.getFirstName() + " " + req.getLastName()
                            + " (" + role.name() + ") demande à rejoindre la plateforme.")
                    .build();
            notifRepo.save(notif);
        });

        return ResponseEntity.ok(Map.of(
                "message", "Votre demande a été envoyée. Votre compte sera activé par l'administrateur."));
    }

    @GetMapping("/inscription/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        return ResponseEntity.ok(Map.of("disponible", !userRepo.existsByUsername(username)));
    }
}

// ══════════════════════════════════════════════════════════════════════════
// PDF CONTROLLER — téléchargement bulletin
// ══════════════════════════════════════════════════════════════════════════

@RestController
@RequestMapping("/api/bulletins")
class BulletinPdfController {

    private final PdfService pdfService;

    public BulletinPdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    /**
     * GET /api/bulletins/{id}/pdf
     * Téléchargement du bulletin en PDF.
     * ENF-2 : étudiant = uniquement son bulletin publié.
     */
    @GetMapping("/{id}/pdf")
    @Audited(action = "DOWNLOAD_BULLETIN_PDF", description = "Téléchargement bulletin PDF")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id,
                                               @AuthenticationPrincipal User user) {
        byte[] pdfBytes = pdfService.genererBulletinPdf(id, user);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"bulletin_" + user.getUsername() + "_" + id + ".pdf\"")
            .body(pdfBytes);
    }
}

// ══════════════════════════════════════════════════════════════════════════
// CSV IMPORT CONTROLLER — import questions en masse
// ══════════════════════════════════════════════════════════════════════════

@RestController
@RequestMapping("/api/questions")
class QuestionImportController {

    private final QuestionRepository questionRepo;
    private final ChoixRepository choixRepo;
    private final MatiereRepository matiereRepo;
    private final com.unz.eval.repository.ImportLogRepository importLogRepo;

    public QuestionImportController(QuestionRepository questionRepo, ChoixRepository choixRepo, MatiereRepository matiereRepo, com.unz.eval.repository.ImportLogRepository importLogRepo) {
        this.questionRepo = questionRepo;
        this.choixRepo = choixRepo;
        this.matiereRepo = matiereRepo;
        this.importLogRepo = importLogRepo;
    }

    /**
     * POST /api/questions/import?matiereId={id}
     * EF-7 : import en masse depuis un fichier CSV.
     *
     * Format CSV attendu (avec en-tête) :
     * enonce,reponse_multiple,points,choix_1,correct_1,choix_2,correct_2,choix_3,correct_3,choix_4,correct_4
     * "Qu'est-ce qu'un algorithme ?",false,1.0,"Recette de cuisine",false,"Suite d'instructions",true,"Un langage",false,"Un ordinateur",false
     */
    @PostMapping("/import")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    @Audited(action = "IMPORT_QUESTIONS_CSV", description = "Import questions depuis CSV")
    public ResponseEntity<?> importCsv(@RequestParam("file") MultipartFile file,
                                        @RequestParam Long matiereId,
                                        @AuthenticationPrincipal User enseignant) {
        if (file.isEmpty()) throw new BadRequestException("Fichier CSV vide.");

        Matiere matiere = matiereRepo.findById(matiereId)
            .orElseThrow(() -> new ResourceNotFoundException("Matière introuvable"));

        // Vérification que l'enseignant enseigne cette matière (v3 : via AffectationEnseignant)
        if (!matiere.estEnseignePar(enseignant))
            throw new org.springframework.security.access.AccessDeniedException(
                "Vous ne pouvez importer des questions que pour vos matières.");

        List<Map<String, Object>> resultats = new ArrayList<>();
        int importees = 0, erreurs = 0;

        try (CSVReader reader = new CSVReader(
                new InputStreamReader(file.getInputStream()))) {

            String[] headers = reader.readNext(); // ignorer l'en-tête
            String[] line;
            int lineNum = 2;

            while ((line = reader.readNext()) != null) {
                try {
                    if (line.length < 5) { erreurs++; continue; }

                    boolean reponseMultiple = Boolean.parseBoolean(line[1].trim());
                    Question question = Question.builder()
                        .enonce(line[0].trim())
                        .type(reponseMultiple ? Question.TypeQuestion.QCM_MULTIPLE : Question.TypeQuestion.QCM_UNIQUE)
                        .points(Double.parseDouble(line[2].trim()))
                        .matiere(matiere)
                        .build();
                    questionRepo.save(question);

                    // Choix : colonnes par paires (texte, estCorrect)
                    int nbChoix = (line.length - 3) / 2;
                    for (int i = 0; i < nbChoix; i++) {
                        int baseIdx = 3 + i * 2;
                        if (baseIdx + 1 >= line.length) break;
                        String texte = line[baseIdx].trim();
                        boolean correct = Boolean.parseBoolean(line[baseIdx + 1].trim());
                        if (!texte.isEmpty()) {
                            choixRepo.save(Choix.builder()
                                .texte(texte).estCorrect(correct).question(question).build());
                        }
                    }
                    importees++;
                } catch (Exception e) {
                    erreurs++;
                    resultats.add(Map.of("ligne", lineNum, "erreur", e.getMessage()));
                }
                lineNum++;
            }
        } catch (Exception e) {
            throw new BadRequestException("Erreur de lecture du fichier CSV : " + e.getMessage());
        }

        // v3 : trace persistante de l'import (remplace le résultat jetable)
        importLogRepo.save(com.unz.eval.entity.ImportLog.builder()
            .matiere(matiere).importePar(enseignant)
            .nbImportees(importees).nbErreurs(erreurs).build());

        return ResponseEntity.ok(Map.of(
            "importees", importees,
            "erreurs", erreurs,
            "details", resultats
        ));
    }
}

// ══════════════════════════════════════════════════════════════════════════
// AUDIT CONTROLLER — consultation des logs (Admin)
// ══════════════════════════════════════════════════════════════════════════

@RestController
@RequestMapping("/api/admin/audit")
@PreAuthorize("hasRole('ADMIN')")
class AuditController {

    private final com.unz.eval.audit.AuditService auditService;

    public AuditController(com.unz.eval.audit.AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<?> getLogs() {
        return ResponseEntity.ok(auditService.getDerniersLogs());
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<?> getLogsByUser(@PathVariable String username) {
        return ResponseEntity.ok(auditService.getLogsParUtilisateur(username));
    }

    @GetMapping("/action/{action}")
    public ResponseEntity<?> getLogsByAction(@PathVariable String action) {
        return ResponseEntity.ok(auditService.getLogsParAction(action));
    }
}
