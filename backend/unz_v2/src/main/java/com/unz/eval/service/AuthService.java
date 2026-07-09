package com.unz.eval.service;

import com.unz.eval.dto.DTOs;
import com.unz.eval.entity.*;
import com.unz.eval.exception.BadRequestException;
import com.unz.eval.exception.ResourceNotFoundException;
import com.unz.eval.repository.AnneeAcademiqueRepository;
import com.unz.eval.repository.ClasseRepository;
import com.unz.eval.repository.InscriptionRepository;
import com.unz.eval.repository.PasswordResetTokenRepository;
import com.unz.eval.repository.RefreshTokenRepository;
import com.unz.eval.repository.UserRepository;
import com.unz.eval.security.PasswordResetToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

/**
 * Le login proprement dit (JWT + refresh token) vit dans AuthControllerV2.
 * Ce service ne gère que la création de compte et la conversion en DTO.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AnneeAcademiqueRepository anneeRepo;
    private final InscriptionRepository inscriptionRepo;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepo;
    private final RefreshTokenRepository refreshTokenRepo;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AnneeAcademiqueRepository anneeRepo,
                        InscriptionRepository inscriptionRepo, EmailService emailService,
                        PasswordResetTokenRepository passwordResetTokenRepo, RefreshTokenRepository refreshTokenRepo) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.anneeRepo = anneeRepo;
        this.inscriptionRepo = inscriptionRepo;
        this.emailService = emailService;
        this.passwordResetTokenRepo = passwordResetTokenRepo;
        this.refreshTokenRepo = refreshTokenRepo;
    }

    /**
     * EF-2 (v3) : un étudiant n'est plus rattaché à une classe directement sur User,
     * mais via une Inscription pour l'année académique active.
     */
    @Transactional
    public User createUser(DTOs.CreateUserRequest req, ClasseRepository classeRepo) {
        if (userRepository.existsByUsername(req.getUsername()))
            throw new BadRequestException("Ce nom d'utilisateur est déjà pris.");
        if (req.getRole() == User.Role.ETUDIANT && req.getClasseId() == null)
            throw new BadRequestException("Un étudiant doit être rattaché à une classe (EF-2).");

        User user = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword())) // ENF-5 : BCrypt
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail())
                .role(req.getRole())
                .build();
        user = userRepository.save(user);

        if (req.getRole() == User.Role.ETUDIANT) {
            Classe classe = classeRepo.findById(req.getClasseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Classe introuvable"));
            AnneeAcademique annee = anneeRepo.findByActiveTrue()
                    .orElseThrow(() -> new BadRequestException(
                            "Aucune année académique active. Contactez l'administrateur."));
            inscriptionRepo.save(Inscription.builder()
                    .etudiant(user).classe(classe).anneeAcademique(annee).build());
        }
        return user;
    }

    public DTOs.UserDTO toUserDTO(User u) {
        String classeNom = anneeRepo.findByActiveTrue()
                .map(u::getClassePour)
                .map(Classe::toString)
                .orElse(null);
        return DTOs.UserDTO.builder()
                .id(u.getId()).username(u.getUsername())
                .firstName(u.getFirstName()).lastName(u.getLastName())
                .email(u.getEmail()).role(u.getRole().name())
                .classeNom(classeNom)
                .build();
    }

    /**
     * Mot de passe oublié (self-service, v4) : si l'e-mail correspond à un compte,
     * on génère un token à usage unique (30 min de validité) et on envoie un lien
     * de réinitialisation par e-mail — le mot de passe n'est PAS changé ici.
     * Renvoie toujours silencieusement dans le cas contraire (sécurité :
     * on ne révèle jamais si un e-mail est enregistré ou non).
     */
    @Transactional
    public void motDePasseOublie(String email) {
        if (email == null || email.isBlank()) return;
        Optional<User> match = userRepository.findByEmailHash(User.hashEmail(email));
        if (match.isEmpty()) {
            log.info("[mot-de-passe-oublié] aucun compte trouvé pour l'e-mail=\"{}\" — aucun envoi (comportement normal si l'e-mail est faux ou inconnu)", email);
            return; // on ne révèle jamais si l'e-mail est inconnu (côté réponse HTTP)
        }
        User user = match.get();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(UUID.randomUUID().toString());
        resetToken.setUser(user);
        resetToken.setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));
        passwordResetTokenRepo.save(resetToken);

        String lien = frontendUrl + "/reinitialiser-mot-de-passe?token=" + resetToken.getToken();
        emailService.envoyerLienReinitialisation(user.getEmail(), user.getFirstName(), lien);
    }

    /**
     * Confirme la réinitialisation : vérifie le token (existe, non utilisé, non expiré),
     * applique le nouveau mot de passe, marque le token comme utilisé et révoque toutes
     * les sessions actives (refresh tokens) par précaution — au cas où le compte aurait
     * été compromis, ça force une reconnexion partout.
     */
    @Transactional
    public void reinitialiserMotDePasse(String token, String nouveauMotDePasse) {
        PasswordResetToken resetToken = passwordResetTokenRepo.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Lien de réinitialisation invalide."));

        if (!resetToken.isValid())
            throw new BadRequestException("Ce lien de réinitialisation a expiré ou a déjà été utilisé. Refaites une demande.");

        if (nouveauMotDePasse == null || nouveauMotDePasse.length() < 6)
            throw new BadRequestException("Le nouveau mot de passe doit contenir au moins 6 caractères.");

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(nouveauMotDePasse));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepo.save(resetToken);

        refreshTokenRepo.findAllByUser(user).forEach(t -> {
            t.setRevoked(true);
            refreshTokenRepo.save(t);
        });
    }

    /** Changement de mot de passe par l'utilisateur connecté, depuis son profil. */
    @Transactional
    public void changerMotDePasse(User user, String ancienMotDePasse, String nouveauMotDePasse) {
        if (!passwordEncoder.matches(ancienMotDePasse, user.getPassword()))
            throw new BadRequestException("L'ancien mot de passe est incorrect.");
        if (nouveauMotDePasse == null || nouveauMotDePasse.length() < 6)
            throw new BadRequestException("Le nouveau mot de passe doit contenir au moins 6 caractères.");
        user.setPassword(passwordEncoder.encode(nouveauMotDePasse));
        userRepository.save(user);
    }

    public String genererMotDePasseTemporaire() {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder("Unz-");
        for (int i = 0; i < 8; i++) sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        return sb.toString();
    }
}
