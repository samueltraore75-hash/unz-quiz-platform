package com.unz.eval.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.unz.eval.entity.User;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Conteneur de tous les DTO de l'application (accès via DTOs.NomDuDto).
 */
public class DTOs {

// ══════════════════════════════════════════════════════════════════════════
// AUTH
// ══════════════════════════════════════════════════════════════════════════

 public static class LoginRequest {

    @NotBlank private String username;
    @NotBlank private String password;

    // getters & setters
    public String getUsername() { return this.username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return this.password; }
    public void setPassword(String password) { this.password = password; }
}
  public static class LoginResponse {

    private String accessToken;
    private String refreshToken;

    private String tokenType = "Bearer";
    private String role;
    private UserDTO user;

    // getters & setters
    public String getAccessToken() { return this.accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return this.refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public String getTokenType() { return this.tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public String getRole() { return this.role; }
    public void setRole(String role) { this.role = role; }
    public UserDTO getUser() { return this.user; }
    public void setUser(UserDTO user) { this.user = user; }

    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static LoginResponseBuilder builder() { return new LoginResponseBuilder(); }

    public static class LoginResponseBuilder {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private String role;
        private UserDTO user;

        public LoginResponseBuilder accessToken(String v) { this.accessToken = v; return this; }
        public LoginResponseBuilder refreshToken(String v) { this.refreshToken = v; return this; }
        public LoginResponseBuilder tokenType(String v) { this.tokenType = v; return this; }
        public LoginResponseBuilder role(String v) { this.role = v; return this; }
        public LoginResponseBuilder user(UserDTO v) { this.user = v; return this; }

        public LoginResponse build() {
            LoginResponse obj = new LoginResponse();
            if (this.accessToken != null) obj.accessToken = this.accessToken;
            if (this.refreshToken != null) obj.refreshToken = this.refreshToken;
            if (this.tokenType != null) obj.tokenType = this.tokenType;
            if (this.role != null) obj.role = this.role;
            if (this.user != null) obj.user = this.user;
            return obj;
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// USER
// ══════════════════════════════════════════════════════════════════════════

/** DTO public — ne contient JAMAIS le mot de passe (ENF-6) */
  @JsonInclude(JsonInclude.Include.NON_NULL)
public static class UserDTO {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String classeNom;

    // getters & setters
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return this.username; }
    public void setUsername(String username) { this.username = username; }
    public String getFirstName() { return this.firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return this.lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return this.email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return this.role; }
    public void setRole(String role) { this.role = role; }
    public String getClasseNom() { return this.classeNom; }
    public void setClasseNom(String classeNom) { this.classeNom = classeNom; }

    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static UserDTOBuilder builder() { return new UserDTOBuilder(); }

    public static class UserDTOBuilder {
        private Long id;
        private String username;
        private String firstName;
        private String lastName;
        private String email;
        private String role;
        private String classeNom;

        public UserDTOBuilder id(Long v) { this.id = v; return this; }
        public UserDTOBuilder username(String v) { this.username = v; return this; }
        public UserDTOBuilder firstName(String v) { this.firstName = v; return this; }
        public UserDTOBuilder lastName(String v) { this.lastName = v; return this; }
        public UserDTOBuilder email(String v) { this.email = v; return this; }
        public UserDTOBuilder role(String v) { this.role = v; return this; }
        public UserDTOBuilder classeNom(String v) { this.classeNom = v; return this; }

        public UserDTO build() {
            UserDTO obj = new UserDTO();
            if (this.id != null) obj.id = this.id;
            if (this.username != null) obj.username = this.username;
            if (this.firstName != null) obj.firstName = this.firstName;
            if (this.lastName != null) obj.lastName = this.lastName;
            if (this.email != null) obj.email = this.email;
            if (this.role != null) obj.role = this.role;
            if (this.classeNom != null) obj.classeNom = this.classeNom;
            return obj;
        }
    }
}

 public static class CreateUserRequest {

    @NotBlank private String username;
    @NotBlank @Size(min = 8) private String password;
    private String firstName;
    private String lastName;
    private String email;
    @NotNull private User.Role role;
    private Long classeId; // obligatoire si role = ETUDIANT

    // getters & setters
    public String getUsername() { return this.username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return this.password; }
    public void setPassword(String password) { this.password = password; }
    public String getFirstName() { return this.firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return this.lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return this.email; }
    public void setEmail(String email) { this.email = email; }
    public Long getClasseId() { return this.classeId; }
    public void setClasseId(Long classeId) { this.classeId = classeId; }
    public User.Role getRole() { return this.role; }
    public void setRole(User.Role role) { this.role = role; }
}

public static class ForgotPasswordRequest {
    @NotBlank @Email private String email;
    public String getEmail() { return this.email; }
    public void setEmail(String email) { this.email = email; }
}

public static class ResetPasswordRequest {
    @NotBlank private String token;
    @NotBlank private String nouveauMotDePasse;
    public String getToken() { return this.token; }
    public void setToken(String v) { this.token = v; }
    public String getNouveauMotDePasse() { return this.nouveauMotDePasse; }
    public void setNouveauMotDePasse(String v) { this.nouveauMotDePasse = v; }
}

public static class ChangePasswordRequest {
    @NotBlank private String ancienMotDePasse;
    @NotBlank private String nouveauMotDePasse;
    public String getAncienMotDePasse() { return this.ancienMotDePasse; }
    public void setAncienMotDePasse(String v) { this.ancienMotDePasse = v; }
    public String getNouveauMotDePasse() { return this.nouveauMotDePasse; }
    public void setNouveauMotDePasse(String v) { this.nouveauMotDePasse = v; }
}

// ══════════════════════════════════════════════════════════════════════════
// QUIZ
// ══════════════════════════════════════════════════════════════════════════

  @JsonInclude(JsonInclude.Include.NON_NULL)
public static class QuizListDTO {

    private Long id;
    private String titre;
    private String matiere;
    private String classe;
    private String typeQuiz;
    private BigDecimal noteSur;
    private Integer dureeMinutes;
    private LocalDateTime dateOuverture;
    private LocalDateTime dateCloture;
    private int nbQuestions;
    private boolean estCloture;

    // getters & setters
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getTitre() { return this.titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getMatiere() { return this.matiere; }
    public void setMatiere(String matiere) { this.matiere = matiere; }
    public String getClasse() { return this.classe; }
    public void setClasse(String classe) { this.classe = classe; }
    public String getTypeQuiz() { return this.typeQuiz; }
    public void setTypeQuiz(String typeQuiz) { this.typeQuiz = typeQuiz; }
    public BigDecimal getNoteSur() { return this.noteSur; }
    public void setNoteSur(BigDecimal noteSur) { this.noteSur = noteSur; }
    public Integer getDureeMinutes() { return this.dureeMinutes; }
    public void setDureeMinutes(Integer dureeMinutes) { this.dureeMinutes = dureeMinutes; }
    public LocalDateTime getDateOuverture() { return this.dateOuverture; }
    public void setDateOuverture(LocalDateTime dateOuverture) { this.dateOuverture = dateOuverture; }
    public LocalDateTime getDateCloture() { return this.dateCloture; }
    public void setDateCloture(LocalDateTime dateCloture) { this.dateCloture = dateCloture; }
    public int getNbQuestions() { return this.nbQuestions; }
    public void setNbQuestions(int nbQuestions) { this.nbQuestions = nbQuestions; }
    public boolean isEstCloture() { return this.estCloture; }
    public void setEstCloture(boolean estCloture) { this.estCloture = estCloture; }

    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static QuizListDTOBuilder builder() { return new QuizListDTOBuilder(); }

    public static class QuizListDTOBuilder {
        private Long id;
        private String titre;
        private String matiere;
        private String classe;
        private String typeQuiz;
        private BigDecimal noteSur;
        private Integer dureeMinutes;
        private LocalDateTime dateOuverture;
        private LocalDateTime dateCloture;
        private Integer nbQuestions;
        private Boolean estCloture;

        public QuizListDTOBuilder id(Long v) { this.id = v; return this; }
        public QuizListDTOBuilder titre(String v) { this.titre = v; return this; }
        public QuizListDTOBuilder matiere(String v) { this.matiere = v; return this; }
        public QuizListDTOBuilder classe(String v) { this.classe = v; return this; }
        public QuizListDTOBuilder typeQuiz(String v) { this.typeQuiz = v; return this; }
        public QuizListDTOBuilder noteSur(BigDecimal v) { this.noteSur = v; return this; }
        public QuizListDTOBuilder dureeMinutes(Integer v) { this.dureeMinutes = v; return this; }
        public QuizListDTOBuilder dateOuverture(LocalDateTime v) { this.dateOuverture = v; return this; }
        public QuizListDTOBuilder dateCloture(LocalDateTime v) { this.dateCloture = v; return this; }
        public QuizListDTOBuilder nbQuestions(Integer v) { this.nbQuestions = v; return this; }
        public QuizListDTOBuilder estCloture(Boolean v) { this.estCloture = v; return this; }

        public QuizListDTO build() {
            QuizListDTO obj = new QuizListDTO();
            if (this.id != null) obj.id = this.id;
            if (this.titre != null) obj.titre = this.titre;
            if (this.matiere != null) obj.matiere = this.matiere;
            if (this.classe != null) obj.classe = this.classe;
            if (this.typeQuiz != null) obj.typeQuiz = this.typeQuiz;
            if (this.noteSur != null) obj.noteSur = this.noteSur;
            if (this.dureeMinutes != null) obj.dureeMinutes = this.dureeMinutes;
            if (this.dateOuverture != null) obj.dateOuverture = this.dateOuverture;
            if (this.dateCloture != null) obj.dateCloture = this.dateCloture;
            if (this.nbQuestions != null) obj.nbQuestions = this.nbQuestions;
            if (this.estCloture != null) obj.estCloture = this.estCloture;
            return obj;
        }
    }
}

  @JsonInclude(JsonInclude.Include.NON_NULL)
public static class QuizDetailDTO {

    private Long id;
    private String titre;
    private String matiere;
    private String typeQuiz;
    private BigDecimal noteSur;
    private Integer dureeMinutes;
    private Integer tentativesMax;
    private LocalDateTime dateCloture;
    private boolean estCloture;
    /** v3.2 : permet à l'étudiant de reprendre une tentative en cours sans dépendre du navigateur */
    private Long tentativeEnCoursId;
    /** v3.2 : nombre de tentatives déjà utilisées par l'étudiant connecté (mode Entraînement) */
    private Integer tentativesUtilisees;
    /** v3.2 : date à partir de laquelle une nouvelle tentative sera possible, si applicable */
    private LocalDateTime prochaineTentativeDisponibleA;
    /** ENF-1 : questions sans les bonnes réponses avant clôture */
    private List<QuestionPublicDTO> questions;
    /** v3.3 : anti-triche — réglages du devoir, transmis au client pour piloter la détection */
    private Integer seuilAlerteScore;
    private boolean pleinEcranObligatoire;

    // getters & setters
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getTitre() { return this.titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getMatiere() { return this.matiere; }
    public void setMatiere(String matiere) { this.matiere = matiere; }
    public String getTypeQuiz() { return this.typeQuiz; }
    public void setTypeQuiz(String typeQuiz) { this.typeQuiz = typeQuiz; }
    public BigDecimal getNoteSur() { return this.noteSur; }
    public void setNoteSur(BigDecimal noteSur) { this.noteSur = noteSur; }
    public Integer getDureeMinutes() { return this.dureeMinutes; }
    public void setDureeMinutes(Integer dureeMinutes) { this.dureeMinutes = dureeMinutes; }
    public Integer getTentativesMax() { return this.tentativesMax; }
    public void setTentativesMax(Integer tentativesMax) { this.tentativesMax = tentativesMax; }
    public LocalDateTime getDateCloture() { return this.dateCloture; }
    public void setDateCloture(LocalDateTime dateCloture) { this.dateCloture = dateCloture; }
    public boolean isEstCloture() { return this.estCloture; }
    public void setEstCloture(boolean estCloture) { this.estCloture = estCloture; }
    public Long getTentativeEnCoursId() { return this.tentativeEnCoursId; }
    public void setTentativeEnCoursId(Long tentativeEnCoursId) { this.tentativeEnCoursId = tentativeEnCoursId; }
    public Integer getTentativesUtilisees() { return this.tentativesUtilisees; }
    public void setTentativesUtilisees(Integer tentativesUtilisees) { this.tentativesUtilisees = tentativesUtilisees; }
    public LocalDateTime getProchaineTentativeDisponibleA() { return this.prochaineTentativeDisponibleA; }
    public void setProchaineTentativeDisponibleA(LocalDateTime prochaineTentativeDisponibleA) { this.prochaineTentativeDisponibleA = prochaineTentativeDisponibleA; }
    public List<QuestionPublicDTO> getQuestions() { return this.questions; }
    public void setQuestions(List<QuestionPublicDTO> questions) { this.questions = questions; }
    public Integer getSeuilAlerteScore() { return this.seuilAlerteScore; }
    public void setSeuilAlerteScore(Integer seuilAlerteScore) { this.seuilAlerteScore = seuilAlerteScore; }
    public boolean isPleinEcranObligatoire() { return this.pleinEcranObligatoire; }
    public void setPleinEcranObligatoire(boolean pleinEcranObligatoire) { this.pleinEcranObligatoire = pleinEcranObligatoire; }

    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static QuizDetailDTOBuilder builder() { return new QuizDetailDTOBuilder(); }

    public static class QuizDetailDTOBuilder {
        private Long id;
        private String titre;
        private String matiere;
        private String typeQuiz;
        private BigDecimal noteSur;
        private Integer dureeMinutes;
        private Integer tentativesMax;
        private LocalDateTime dateCloture;
        private Boolean estCloture;
        private Long tentativeEnCoursId;
        private Integer tentativesUtilisees;
        private LocalDateTime prochaineTentativeDisponibleA;
        private List<QuestionPublicDTO> questions;
        private Integer seuilAlerteScore;
        private Boolean pleinEcranObligatoire;

        public QuizDetailDTOBuilder id(Long v) { this.id = v; return this; }
        public QuizDetailDTOBuilder titre(String v) { this.titre = v; return this; }
        public QuizDetailDTOBuilder matiere(String v) { this.matiere = v; return this; }
        public QuizDetailDTOBuilder typeQuiz(String v) { this.typeQuiz = v; return this; }
        public QuizDetailDTOBuilder noteSur(BigDecimal v) { this.noteSur = v; return this; }
        public QuizDetailDTOBuilder dureeMinutes(Integer v) { this.dureeMinutes = v; return this; }
        public QuizDetailDTOBuilder tentativesMax(Integer v) { this.tentativesMax = v; return this; }
        public QuizDetailDTOBuilder dateCloture(LocalDateTime v) { this.dateCloture = v; return this; }
        public QuizDetailDTOBuilder estCloture(Boolean v) { this.estCloture = v; return this; }
        public QuizDetailDTOBuilder tentativeEnCoursId(Long v) { this.tentativeEnCoursId = v; return this; }
        public QuizDetailDTOBuilder tentativesUtilisees(Integer v) { this.tentativesUtilisees = v; return this; }
        public QuizDetailDTOBuilder prochaineTentativeDisponibleA(LocalDateTime v) { this.prochaineTentativeDisponibleA = v; return this; }
        public QuizDetailDTOBuilder questions(List<QuestionPublicDTO> v) { this.questions = v; return this; }
        public QuizDetailDTOBuilder seuilAlerteScore(Integer v) { this.seuilAlerteScore = v; return this; }
        public QuizDetailDTOBuilder pleinEcranObligatoire(Boolean v) { this.pleinEcranObligatoire = v; return this; }

        public QuizDetailDTO build() {
            QuizDetailDTO obj = new QuizDetailDTO();
            if (this.id != null) obj.id = this.id;
            if (this.titre != null) obj.titre = this.titre;
            if (this.matiere != null) obj.matiere = this.matiere;
            if (this.typeQuiz != null) obj.typeQuiz = this.typeQuiz;
            if (this.noteSur != null) obj.noteSur = this.noteSur;
            if (this.dureeMinutes != null) obj.dureeMinutes = this.dureeMinutes;
            if (this.tentativesMax != null) obj.tentativesMax = this.tentativesMax;
            if (this.dateCloture != null) obj.dateCloture = this.dateCloture;
            if (this.estCloture != null) obj.estCloture = this.estCloture;
            if (this.tentativeEnCoursId != null) obj.tentativeEnCoursId = this.tentativeEnCoursId;
            if (this.tentativesUtilisees != null) obj.tentativesUtilisees = this.tentativesUtilisees;
            if (this.prochaineTentativeDisponibleA != null) obj.prochaineTentativeDisponibleA = this.prochaineTentativeDisponibleA;
            if (this.questions != null) obj.questions = this.questions;
            if (this.seuilAlerteScore != null) obj.seuilAlerteScore = this.seuilAlerteScore;
            if (this.pleinEcranObligatoire != null) obj.pleinEcranObligatoire = this.pleinEcranObligatoire;
            return obj;
        }
    }
}

 public static class CreateQuizRequest {

    @NotBlank private String titre;
    @NotNull private Long matiereId;
    @NotNull private Long classeId;
    @NotNull private String typeQuiz;
    @NotNull @DecimalMin("1") private BigDecimal noteSur = BigDecimal.valueOf(20);
    @Min(1) @Max(300) private int dureeMinutes = 30;
    @Min(1) private int tentativesMax = 1;
    private int delaiEntreTentativesMinutes = 0;
    private LocalDateTime dateOuverture;
    private LocalDateTime dateCloture;
    /** v3.3 : anti-triche — réglages optionnels, valeurs par défaut raisonnables si absents */
    private Integer seuilAlerteScore;
    private Boolean pleinEcranObligatoire;

    // getters & setters
    public String getTitre() { return this.titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public Long getMatiereId() { return this.matiereId; }
    public void setMatiereId(Long matiereId) { this.matiereId = matiereId; }
    public Long getClasseId() { return this.classeId; }
    public void setClasseId(Long classeId) { this.classeId = classeId; }
    public String getTypeQuiz() { return this.typeQuiz; }
    public void setTypeQuiz(String typeQuiz) { this.typeQuiz = typeQuiz; }
    public BigDecimal getNoteSur() { return this.noteSur; }
    public void setNoteSur(BigDecimal noteSur) { this.noteSur = noteSur; }
    public int getDureeMinutes() { return this.dureeMinutes; }
    public void setDureeMinutes(int dureeMinutes) { this.dureeMinutes = dureeMinutes; }
    public int getTentativesMax() { return this.tentativesMax; }
    public void setTentativesMax(int tentativesMax) { this.tentativesMax = tentativesMax; }
    public int getDelaiEntreTentativesMinutes() { return this.delaiEntreTentativesMinutes; }
    public void setDelaiEntreTentativesMinutes(int delaiEntreTentativesMinutes) { this.delaiEntreTentativesMinutes = delaiEntreTentativesMinutes; }
    public LocalDateTime getDateOuverture() { return this.dateOuverture; }
    public void setDateOuverture(LocalDateTime dateOuverture) { this.dateOuverture = dateOuverture; }
    public LocalDateTime getDateCloture() { return this.dateCloture; }
    public void setDateCloture(LocalDateTime dateCloture) { this.dateCloture = dateCloture; }
    public Integer getSeuilAlerteScore() { return this.seuilAlerteScore; }
    public void setSeuilAlerteScore(Integer seuilAlerteScore) { this.seuilAlerteScore = seuilAlerteScore; }
    public Boolean getPleinEcranObligatoire() { return this.pleinEcranObligatoire; }
    public void setPleinEcranObligatoire(Boolean pleinEcranObligatoire) { this.pleinEcranObligatoire = pleinEcranObligatoire; }
}

// ══════════════════════════════════════════════════════════════════════════
// QUESTIONS & CHOIX
// ENF-1 : deux versions — public (sans estCorrect) et corrigé (avec estCorrect)
// ══════════════════════════════════════════════════════════════════════════

/** ENF-1 : version publique — pas de champ estCorrect */
 public static class ChoixPublicDTO {

    private Long id;
    private String texte;
    // estCorrect VOLONTAIREMENT ABSENT (ENF-1)

    // getters & setters
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getTexte() { return this.texte; }
    public void setTexte(String texte) { this.texte = texte; }

    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static ChoixPublicDTOBuilder builder() { return new ChoixPublicDTOBuilder(); }

    public static class ChoixPublicDTOBuilder {
        private Long id;
        private String texte;

        public ChoixPublicDTOBuilder id(Long v) { this.id = v; return this; }
        public ChoixPublicDTOBuilder texte(String v) { this.texte = v; return this; }

        public ChoixPublicDTO build() {
            ChoixPublicDTO obj = new ChoixPublicDTO();
            if (this.id != null) obj.id = this.id;
            if (this.texte != null) obj.texte = this.texte;
            return obj;
        }
    }
}

/** Version corrigée — uniquement après clôture validée (EF-11) */
 public static class ChoixCorrigeDTO {

    private Long id;
    private String texte;
    private boolean estCorrect; // exposé UNIQUEMENT après clôture

    // getters & setters
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getTexte() { return this.texte; }
    public void setTexte(String texte) { this.texte = texte; }
    public boolean isEstCorrect() { return this.estCorrect; }
    public void setEstCorrect(boolean estCorrect) { this.estCorrect = estCorrect; }

    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static ChoixCorrigeDTOBuilder builder() { return new ChoixCorrigeDTOBuilder(); }

    public static class ChoixCorrigeDTOBuilder {
        private Long id;
        private String texte;
        private Boolean estCorrect;

        public ChoixCorrigeDTOBuilder id(Long v) { this.id = v; return this; }
        public ChoixCorrigeDTOBuilder texte(String v) { this.texte = v; return this; }
        public ChoixCorrigeDTOBuilder estCorrect(Boolean v) { this.estCorrect = v; return this; }

        public ChoixCorrigeDTO build() {
            ChoixCorrigeDTO obj = new ChoixCorrigeDTO();
            if (this.id != null) obj.id = this.id;
            if (this.texte != null) obj.texte = this.texte;
            if (this.estCorrect != null) obj.estCorrect = this.estCorrect;
            return obj;
        }
    }
}

 public static class QuestionPublicDTO {

    private Long id;
    private String enonce;
    /** v3.2 : QCM_UNIQUE, QCM_MULTIPLE, VRAI_FAUX, REPONSE_COURTE — indispensable pour bien rendre la question */
    private String type;
    private boolean reponseMultiple;
    private double points;
    private List<ChoixPublicDTO> choix; // ENF-1 : sans estCorrect — vide pour REPONSE_COURTE

    // getters & setters
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getEnonce() { return this.enonce; }
    public void setEnonce(String enonce) { this.enonce = enonce; }
    public String getType() { return this.type; }
    public void setType(String type) { this.type = type; }
    public boolean isReponseMultiple() { return this.reponseMultiple; }
    public void setReponseMultiple(boolean reponseMultiple) { this.reponseMultiple = reponseMultiple; }
    public double getPoints() { return this.points; }
    public void setPoints(double points) { this.points = points; }
    public List<ChoixPublicDTO> getChoix() { return this.choix; }
    public void setChoix(List<ChoixPublicDTO> choix) { this.choix = choix; }

    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static QuestionPublicDTOBuilder builder() { return new QuestionPublicDTOBuilder(); }

    public static class QuestionPublicDTOBuilder {
        private Long id;
        private String enonce;
        private String type;
        private Boolean reponseMultiple;
        private Double points;
        private List<ChoixPublicDTO> choix;

        public QuestionPublicDTOBuilder id(Long v) { this.id = v; return this; }
        public QuestionPublicDTOBuilder enonce(String v) { this.enonce = v; return this; }
        public QuestionPublicDTOBuilder type(String v) { this.type = v; return this; }
        public QuestionPublicDTOBuilder reponseMultiple(Boolean v) { this.reponseMultiple = v; return this; }
        public QuestionPublicDTOBuilder points(Double v) { this.points = v; return this; }
        public QuestionPublicDTOBuilder choix(List<ChoixPublicDTO> v) { this.choix = v; return this; }

        public QuestionPublicDTO build() {
            QuestionPublicDTO obj = new QuestionPublicDTO();
            if (this.id != null) obj.id = this.id;
            if (this.enonce != null) obj.enonce = this.enonce;
            if (this.type != null) obj.type = this.type;
            if (this.reponseMultiple != null) obj.reponseMultiple = this.reponseMultiple;
            if (this.points != null) obj.points = this.points;
            if (this.choix != null) obj.choix = this.choix;
            return obj;
        }
    }
}

 public static class QuestionCorrigeeDTO {

    private Long id;
    private String enonce;
    private String type;
    /** v3.2 : explication pédagogique affichée après correction, si renseignée par l'enseignant */
    private String explication;
    private List<ChoixCorrigeDTO> choix; // avec estCorrect
    /** v3.2 : réponse libre de l'étudiant, pour les questions REPONSE_COURTE */
    private String reponseTexteEtudiant;
    private boolean estCorrecte;
    /** v3.2 : true pour REPONSE_COURTE — la correction automatique ne s'applique pas, à corriger manuellement */
    private boolean correctionManuelle;

    // getters & setters
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getEnonce() { return this.enonce; }
    public void setEnonce(String enonce) { this.enonce = enonce; }
    public String getType() { return this.type; }
    public void setType(String type) { this.type = type; }
    public String getExplication() { return this.explication; }
    public void setExplication(String explication) { this.explication = explication; }
    public List<ChoixCorrigeDTO> getChoix() { return this.choix; }
    public void setChoix(List<ChoixCorrigeDTO> choix) { this.choix = choix; }
    public String getReponseTexteEtudiant() { return this.reponseTexteEtudiant; }
    public void setReponseTexteEtudiant(String reponseTexteEtudiant) { this.reponseTexteEtudiant = reponseTexteEtudiant; }
    public boolean isEstCorrecte() { return this.estCorrecte; }
    public void setEstCorrecte(boolean estCorrecte) { this.estCorrecte = estCorrecte; }
    public boolean isCorrectionManuelle() { return this.correctionManuelle; }
    public void setCorrectionManuelle(boolean correctionManuelle) { this.correctionManuelle = correctionManuelle; }

    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static QuestionCorrigeeDTOBuilder builder() { return new QuestionCorrigeeDTOBuilder(); }

    public static class QuestionCorrigeeDTOBuilder {
        private Long id;
        private String enonce;
        private String type;
        private String explication;
        private List<ChoixCorrigeDTO> choix;
        private String reponseTexteEtudiant;
        private Boolean estCorrecte;
        private Boolean correctionManuelle;

        public QuestionCorrigeeDTOBuilder id(Long v) { this.id = v; return this; }
        public QuestionCorrigeeDTOBuilder enonce(String v) { this.enonce = v; return this; }
        public QuestionCorrigeeDTOBuilder type(String v) { this.type = v; return this; }
        public QuestionCorrigeeDTOBuilder explication(String v) { this.explication = v; return this; }
        public QuestionCorrigeeDTOBuilder choix(List<ChoixCorrigeDTO> v) { this.choix = v; return this; }
        public QuestionCorrigeeDTOBuilder reponseTexteEtudiant(String v) { this.reponseTexteEtudiant = v; return this; }
        public QuestionCorrigeeDTOBuilder estCorrecte(Boolean v) { this.estCorrecte = v; return this; }
        public QuestionCorrigeeDTOBuilder correctionManuelle(Boolean v) { this.correctionManuelle = v; return this; }

        public QuestionCorrigeeDTO build() {
            QuestionCorrigeeDTO obj = new QuestionCorrigeeDTO();
            if (this.id != null) obj.id = this.id;
            if (this.enonce != null) obj.enonce = this.enonce;
            if (this.type != null) obj.type = this.type;
            if (this.explication != null) obj.explication = this.explication;
            if (this.choix != null) obj.choix = this.choix;
            if (this.reponseTexteEtudiant != null) obj.reponseTexteEtudiant = this.reponseTexteEtudiant;
            if (this.estCorrecte != null) obj.estCorrecte = this.estCorrecte;
            if (this.correctionManuelle != null) obj.correctionManuelle = this.correctionManuelle;
            return obj;
        }
    }
}

 public static class CreateQuestionRequest {

    @NotBlank private String enonce;
    private boolean reponseMultiple = false;
    @Min(0) private double points = 1.0;
    @NotNull private Long matiereId;
    @NotEmpty private List<CreateChoixRequest> choix;

    // getters & setters
    public String getEnonce() { return this.enonce; }
    public void setEnonce(String enonce) { this.enonce = enonce; }
    public boolean isReponseMultiple() { return this.reponseMultiple; }
    public void setReponseMultiple(boolean reponseMultiple) { this.reponseMultiple = reponseMultiple; }
    public double getPoints() { return this.points; }
    public void setPoints(double points) { this.points = points; }
    public Long getMatiereId() { return this.matiereId; }
    public void setMatiereId(Long matiereId) { this.matiereId = matiereId; }
    public List<CreateChoixRequest> getChoix() { return this.choix; }
    public void setChoix(List<CreateChoixRequest> choix) { this.choix = choix; }
}

 public static class CreateChoixRequest {

    @NotBlank private String texte;
    private boolean estCorrect = false;

    // getters & setters
    public String getTexte() { return this.texte; }
    public void setTexte(String texte) { this.texte = texte; }
    public boolean isEstCorrect() { return this.estCorrect; }
    public void setEstCorrect(boolean estCorrect) { this.estCorrect = estCorrect; }
}

 public static class AddQuestionsRequest {

    @NotEmpty private List<Long> questionIds;

    // getters & setters
    public List<Long> getQuestionIds() { return this.questionIds; }
    public void setQuestionIds(List<Long> questionIds) { this.questionIds = questionIds; }
}

// ══════════════════════════════════════════════════════════════════════════
// TENTATIVES & RÉSULTATS
// ══════════════════════════════════════════════════════════════════════════

 public static class SubmitReponseRequest {

    @NotNull private Long questionId;
    private List<Long> choixIds;
    /** v3.2 : réponse libre pour les questions REPONSE_COURTE */
    private String reponseTexte;

    // getters & setters
    public Long getQuestionId() { return this.questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public List<Long> getChoixIds() { return this.choixIds; }
    public void setChoixIds(List<Long> choixIds) { this.choixIds = choixIds; }
    public String getReponseTexte() { return this.reponseTexte; }
    public void setReponseTexte(String reponseTexte) { this.reponseTexte = reponseTexte; }
}

/** v3.3 : anti-triche — l'étudiant signale un événement suspect détecté côté client */
 public static class SignalerEvenementRequest {

    @NotNull private String type;

    // getters & setters
    public String getType() { return this.type; }
    public void setType(String type) { this.type = type; }
}

/** v3.2 : reprise d'une tentative en cours — restaure les réponses déjà sauvegardées */
 public static class TentativeEnCoursDTO {

    private Long id;
    private Long quizId;
    private LocalDateTime dateDebut;
    private Integer dureeMinutes;
    private List<SubmitReponseRequest> reponses;

    // getters & setters
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public Long getQuizId() { return this.quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }
    public LocalDateTime getDateDebut() { return this.dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }
    public Integer getDureeMinutes() { return this.dureeMinutes; }
    public void setDureeMinutes(Integer dureeMinutes) { this.dureeMinutes = dureeMinutes; }
    public List<SubmitReponseRequest> getReponses() { return this.reponses; }
    public void setReponses(List<SubmitReponseRequest> reponses) { this.reponses = reponses; }

    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static TentativeEnCoursDTOBuilder builder() { return new TentativeEnCoursDTOBuilder(); }

    public static class TentativeEnCoursDTOBuilder {
        private Long id;
        private Long quizId;
        private LocalDateTime dateDebut;
        private Integer dureeMinutes;
        private List<SubmitReponseRequest> reponses;

        public TentativeEnCoursDTOBuilder id(Long v) { this.id = v; return this; }
        public TentativeEnCoursDTOBuilder quizId(Long v) { this.quizId = v; return this; }
        public TentativeEnCoursDTOBuilder dateDebut(LocalDateTime v) { this.dateDebut = v; return this; }
        public TentativeEnCoursDTOBuilder dureeMinutes(Integer v) { this.dureeMinutes = v; return this; }
        public TentativeEnCoursDTOBuilder reponses(List<SubmitReponseRequest> v) { this.reponses = v; return this; }

        public TentativeEnCoursDTO build() {
            TentativeEnCoursDTO obj = new TentativeEnCoursDTO();
            if (this.id != null) obj.id = this.id;
            if (this.quizId != null) obj.quizId = this.quizId;
            if (this.dateDebut != null) obj.dateDebut = this.dateDebut;
            if (this.dureeMinutes != null) obj.dureeMinutes = this.dureeMinutes;
            if (this.reponses != null) obj.reponses = this.reponses;
            return obj;
        }
    }
}

  @JsonInclude(JsonInclude.Include.NON_NULL)
public static class TentativeResultDTO {

    private Long id;
    private String quizTitre;
    private String matiere;
    private LocalDateTime dateDebut;
    private LocalDateTime dateSoumission;
    /** EF-10 : null si quiz pas encore clôturé */
    private BigDecimal noteObtenue;
    private Double baremeTotal;
    private Double noteSur20;
    /** v3.1 : score et dénominateur exprimés dans le barème déclaré par l'enseignant (20, 40, 50…) */
    private Double noteSurBareme;
    private BigDecimal bareme;
    /** EF-11 : détail des réponses — uniquement après clôture */
    private List<QuestionCorrigeeDTO> reponses;
    /** v3.3 : anti-triche — transparence totale, l'étudiant voit aussi ce qui a été enregistré */
    private long nbEvenementsSuspects;

    // getters & setters
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getQuizTitre() { return this.quizTitre; }
    public void setQuizTitre(String quizTitre) { this.quizTitre = quizTitre; }
    public String getMatiere() { return this.matiere; }
    public void setMatiere(String matiere) { this.matiere = matiere; }
    public LocalDateTime getDateDebut() { return this.dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }
    public LocalDateTime getDateSoumission() { return this.dateSoumission; }
    public void setDateSoumission(LocalDateTime dateSoumission) { this.dateSoumission = dateSoumission; }
    public BigDecimal getNoteObtenue() { return this.noteObtenue; }
    public void setNoteObtenue(BigDecimal noteObtenue) { this.noteObtenue = noteObtenue; }
    public Double getBaremeTotal() { return this.baremeTotal; }
    public void setBaremeTotal(Double baremeTotal) { this.baremeTotal = baremeTotal; }
    public Double getNoteSur20() { return this.noteSur20; }
    public void setNoteSur20(Double noteSur20) { this.noteSur20 = noteSur20; }
    public Double getNoteSurBareme() { return this.noteSurBareme; }
    public void setNoteSurBareme(Double noteSurBareme) { this.noteSurBareme = noteSurBareme; }
    public BigDecimal getBareme() { return this.bareme; }
    public void setBareme(BigDecimal bareme) { this.bareme = bareme; }
    public List<QuestionCorrigeeDTO> getReponses() { return this.reponses; }
    public void setReponses(List<QuestionCorrigeeDTO> reponses) { this.reponses = reponses; }
    public long getNbEvenementsSuspects() { return this.nbEvenementsSuspects; }
    public void setNbEvenementsSuspects(long nbEvenementsSuspects) { this.nbEvenementsSuspects = nbEvenementsSuspects; }

    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static TentativeResultDTOBuilder builder() { return new TentativeResultDTOBuilder(); }

    public static class TentativeResultDTOBuilder {
        private Long id;
        private String quizTitre;
        private String matiere;
        private LocalDateTime dateDebut;
        private LocalDateTime dateSoumission;
        private BigDecimal noteObtenue;
        private Double baremeTotal;
        private Double noteSur20;
        private Double noteSurBareme;
        private BigDecimal bareme;
        private List<QuestionCorrigeeDTO> reponses;
        private long nbEvenementsSuspects;

        public TentativeResultDTOBuilder id(Long v) { this.id = v; return this; }
        public TentativeResultDTOBuilder quizTitre(String v) { this.quizTitre = v; return this; }
        public TentativeResultDTOBuilder matiere(String v) { this.matiere = v; return this; }
        public TentativeResultDTOBuilder dateDebut(LocalDateTime v) { this.dateDebut = v; return this; }
        public TentativeResultDTOBuilder dateSoumission(LocalDateTime v) { this.dateSoumission = v; return this; }
        public TentativeResultDTOBuilder noteObtenue(BigDecimal v) { this.noteObtenue = v; return this; }
        public TentativeResultDTOBuilder baremeTotal(Double v) { this.baremeTotal = v; return this; }
        public TentativeResultDTOBuilder noteSur20(Double v) { this.noteSur20 = v; return this; }
        public TentativeResultDTOBuilder noteSurBareme(Double v) { this.noteSurBareme = v; return this; }
        public TentativeResultDTOBuilder bareme(BigDecimal v) { this.bareme = v; return this; }
        public TentativeResultDTOBuilder reponses(List<QuestionCorrigeeDTO> v) { this.reponses = v; return this; }
        public TentativeResultDTOBuilder nbEvenementsSuspects(long v) { this.nbEvenementsSuspects = v; return this; }

        public TentativeResultDTO build() {
            TentativeResultDTO obj = new TentativeResultDTO();
            if (this.id != null) obj.id = this.id;
            if (this.quizTitre != null) obj.quizTitre = this.quizTitre;
            if (this.matiere != null) obj.matiere = this.matiere;
            if (this.dateDebut != null) obj.dateDebut = this.dateDebut;
            if (this.dateSoumission != null) obj.dateSoumission = this.dateSoumission;
            if (this.noteObtenue != null) obj.noteObtenue = this.noteObtenue;
            if (this.baremeTotal != null) obj.baremeTotal = this.baremeTotal;
            if (this.noteSur20 != null) obj.noteSur20 = this.noteSur20;
            if (this.noteSurBareme != null) obj.noteSurBareme = this.noteSurBareme;
            if (this.bareme != null) obj.bareme = this.bareme;
            if (this.reponses != null) obj.reponses = this.reponses;
            obj.nbEvenementsSuspects = this.nbEvenementsSuspects;
            return obj;
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// STATS ANONYMISÉES (EF-14 / ENF-3 / ENF-4)
// ══════════════════════════════════════════════════════════════════════════

/**
 * ENF-3 : aucun identifiant individuel — uniquement des agrégats.
 */
 public static class StatsQuestionDTO {

    private Long questionId;
    private String enonce;
    private double points;
    private long nbReponsesTotales;
    private double tauxReussite; // en %
    private List<StatsChoixDTO> repartitionChoix;
    // Évolution entre 1re et tentatives suivantes
    private Double tauxReussiteT1;
    private Double tauxReussiteTentativesSuivantes;

    // getters & setters
    public Long getQuestionId() { return this.questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getEnonce() { return this.enonce; }
    public void setEnonce(String enonce) { this.enonce = enonce; }
    public double getPoints() { return this.points; }
    public void setPoints(double points) { this.points = points; }
    public long getNbReponsesTotales() { return this.nbReponsesTotales; }
    public void setNbReponsesTotales(long nbReponsesTotales) { this.nbReponsesTotales = nbReponsesTotales; }
    public double getTauxReussite() { return this.tauxReussite; }
    public void setTauxReussite(double tauxReussite) { this.tauxReussite = tauxReussite; }
    public List<StatsChoixDTO> getRepartitionChoix() { return this.repartitionChoix; }
    public void setRepartitionChoix(List<StatsChoixDTO> repartitionChoix) { this.repartitionChoix = repartitionChoix; }
    public Double getTauxReussiteT1() { return this.tauxReussiteT1; }
    public void setTauxReussiteT1(Double tauxReussiteT1) { this.tauxReussiteT1 = tauxReussiteT1; }
    public Double getTauxReussiteTentativesSuivantes() { return this.tauxReussiteTentativesSuivantes; }
    public void setTauxReussiteTentativesSuivantes(Double tauxReussiteTentativesSuivantes) { this.tauxReussiteTentativesSuivantes = tauxReussiteTentativesSuivantes; }

    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static StatsQuestionDTOBuilder builder() { return new StatsQuestionDTOBuilder(); }

    public static class StatsQuestionDTOBuilder {
        private Long questionId;
        private String enonce;
        private Double points;
        private Long nbReponsesTotales;
        private Double tauxReussite;
        private List<StatsChoixDTO> repartitionChoix;
        private Double tauxReussiteT1;
        private Double tauxReussiteTentativesSuivantes;

        public StatsQuestionDTOBuilder questionId(Long v) { this.questionId = v; return this; }
        public StatsQuestionDTOBuilder enonce(String v) { this.enonce = v; return this; }
        public StatsQuestionDTOBuilder points(Double v) { this.points = v; return this; }
        public StatsQuestionDTOBuilder nbReponsesTotales(Long v) { this.nbReponsesTotales = v; return this; }
        public StatsQuestionDTOBuilder tauxReussite(Double v) { this.tauxReussite = v; return this; }
        public StatsQuestionDTOBuilder repartitionChoix(List<StatsChoixDTO> v) { this.repartitionChoix = v; return this; }
        public StatsQuestionDTOBuilder tauxReussiteT1(Double v) { this.tauxReussiteT1 = v; return this; }
        public StatsQuestionDTOBuilder tauxReussiteTentativesSuivantes(Double v) { this.tauxReussiteTentativesSuivantes = v; return this; }

        public StatsQuestionDTO build() {
            StatsQuestionDTO obj = new StatsQuestionDTO();
            if (this.questionId != null) obj.questionId = this.questionId;
            if (this.enonce != null) obj.enonce = this.enonce;
            if (this.points != null) obj.points = this.points;
            if (this.nbReponsesTotales != null) obj.nbReponsesTotales = this.nbReponsesTotales;
            if (this.tauxReussite != null) obj.tauxReussite = this.tauxReussite;
            if (this.repartitionChoix != null) obj.repartitionChoix = this.repartitionChoix;
            if (this.tauxReussiteT1 != null) obj.tauxReussiteT1 = this.tauxReussiteT1;
            if (this.tauxReussiteTentativesSuivantes != null) obj.tauxReussiteTentativesSuivantes = this.tauxReussiteTentativesSuivantes;
            return obj;
        }
    }
}

 public static class StatsChoixDTO {

    private Long choixId;
    private String texte;
    private boolean estCorrect;
    private long nbSelections;
    private double pourcentage;

    // getters & setters
    public Long getChoixId() { return this.choixId; }
    public void setChoixId(Long choixId) { this.choixId = choixId; }
    public String getTexte() { return this.texte; }
    public void setTexte(String texte) { this.texte = texte; }
    public boolean isEstCorrect() { return this.estCorrect; }
    public void setEstCorrect(boolean estCorrect) { this.estCorrect = estCorrect; }
    public long getNbSelections() { return this.nbSelections; }
    public void setNbSelections(long nbSelections) { this.nbSelections = nbSelections; }
    public double getPourcentage() { return this.pourcentage; }
    public void setPourcentage(double pourcentage) { this.pourcentage = pourcentage; }

    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static StatsChoixDTOBuilder builder() { return new StatsChoixDTOBuilder(); }

    public static class StatsChoixDTOBuilder {
        private Long choixId;
        private String texte;
        private Boolean estCorrect;
        private Long nbSelections;
        private Double pourcentage;

        public StatsChoixDTOBuilder choixId(Long v) { this.choixId = v; return this; }
        public StatsChoixDTOBuilder texte(String v) { this.texte = v; return this; }
        public StatsChoixDTOBuilder estCorrect(Boolean v) { this.estCorrect = v; return this; }
        public StatsChoixDTOBuilder nbSelections(Long v) { this.nbSelections = v; return this; }
        public StatsChoixDTOBuilder pourcentage(Double v) { this.pourcentage = v; return this; }

        public StatsChoixDTO build() {
            StatsChoixDTO obj = new StatsChoixDTO();
            if (this.choixId != null) obj.choixId = this.choixId;
            if (this.texte != null) obj.texte = this.texte;
            if (this.estCorrect != null) obj.estCorrect = this.estCorrect;
            if (this.nbSelections != null) obj.nbSelections = this.nbSelections;
            if (this.pourcentage != null) obj.pourcentage = this.pourcentage;
            return obj;
        }
    }
}

 public static class QuizStatsDTO {

    private String quizTitre;
    // ENF-3 : liste de stats, jamais de données individuelles
    private List<StatsQuestionDTO> statistiques;

    // getters & setters
    public String getQuizTitre() { return this.quizTitre; }
    public void setQuizTitre(String quizTitre) { this.quizTitre = quizTitre; }
    public List<StatsQuestionDTO> getStatistiques() { return this.statistiques; }
    public void setStatistiques(List<StatsQuestionDTO> statistiques) { this.statistiques = statistiques; }

    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static QuizStatsDTOBuilder builder() { return new QuizStatsDTOBuilder(); }

    public static class QuizStatsDTOBuilder {
        private String quizTitre;
        private List<StatsQuestionDTO> statistiques;

        public QuizStatsDTOBuilder quizTitre(String v) { this.quizTitre = v; return this; }
        public QuizStatsDTOBuilder statistiques(List<StatsQuestionDTO> v) { this.statistiques = v; return this; }

        public QuizStatsDTO build() {
            QuizStatsDTO obj = new QuizStatsDTO();
            if (this.quizTitre != null) obj.quizTitre = this.quizTitre;
            if (this.statistiques != null) obj.statistiques = this.statistiques;
            return obj;
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// NOTES & BULLETINS
// ══════════════════════════════════════════════════════════════════════════

 public static class CreateNoteRequest {

    @NotNull private Long etudiantId;
    @NotBlank private String typeNote; // EXAMEN_ECRIT, TP, CONTROLE_CONTINU
    @NotNull @DecimalMin("0") @DecimalMax("20") private BigDecimal valeur;
    @NotNull @DecimalMin("0.1") private BigDecimal ponderation;

    // getters & setters
    public Long getEtudiantId() { return this.etudiantId; }
    public void setEtudiantId(Long etudiantId) { this.etudiantId = etudiantId; }
    public String getTypeNote() { return this.typeNote; }
    public void setTypeNote(String typeNote) { this.typeNote = typeNote; }
    public BigDecimal getValeur() { return this.valeur; }
    public void setValeur(BigDecimal valeur) { this.valeur = valeur; }
    public BigDecimal getPonderation() { return this.ponderation; }
    public void setPonderation(BigDecimal ponderation) { this.ponderation = ponderation; }
}

 public static class GenerateBulletinRequest {

    @NotNull private Long etudiantId;
    @NotNull private Long semestreId;
    private boolean publier = false;

    // getters & setters
    public Long getEtudiantId() { return this.etudiantId; }
    public void setEtudiantId(Long etudiantId) { this.etudiantId = etudiantId; }
    public Long getSemestreId() { return this.semestreId; }
    public void setSemestreId(Long semestreId) { this.semestreId = semestreId; }
    public boolean isPublier() { return this.publier; }
    public void setPublier(boolean publier) { this.publier = publier; }
}

  @JsonInclude(JsonInclude.Include.NON_NULL)
public static class BulletinDTO {

    private Long id;
    private String etudiantNom;
    private String semestreLabel;
    private BigDecimal moyenneGenerale;
    private int creditsAcquis;
    private int creditsTotal;
    private boolean semestreValide;
    private boolean publie;
    private LocalDateTime datePublication;
    private List<UeDetailDTO> detailUes;

    // getters & setters
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getEtudiantNom() { return this.etudiantNom; }
    public void setEtudiantNom(String etudiantNom) { this.etudiantNom = etudiantNom; }
    public String getSemestreLabel() { return this.semestreLabel; }
    public void setSemestreLabel(String semestreLabel) { this.semestreLabel = semestreLabel; }
    public BigDecimal getMoyenneGenerale() { return this.moyenneGenerale; }
    public void setMoyenneGenerale(BigDecimal moyenneGenerale) { this.moyenneGenerale = moyenneGenerale; }
    public int getCreditsAcquis() { return this.creditsAcquis; }
    public void setCreditsAcquis(int creditsAcquis) { this.creditsAcquis = creditsAcquis; }
    public int getCreditsTotal() { return this.creditsTotal; }
    public void setCreditsTotal(int creditsTotal) { this.creditsTotal = creditsTotal; }
    public boolean isSemestreValide() { return this.semestreValide; }
    public void setSemestreValide(boolean semestreValide) { this.semestreValide = semestreValide; }
    public boolean isPublie() { return this.publie; }
    public void setPublie(boolean publie) { this.publie = publie; }
    public LocalDateTime getDatePublication() { return this.datePublication; }
    public void setDatePublication(LocalDateTime datePublication) { this.datePublication = datePublication; }
    public List<UeDetailDTO> getDetailUes() { return this.detailUes; }
    public void setDetailUes(List<UeDetailDTO> detailUes) { this.detailUes = detailUes; }

    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static BulletinDTOBuilder builder() { return new BulletinDTOBuilder(); }

    public static class BulletinDTOBuilder {
        private Long id;
        private String etudiantNom;
        private String semestreLabel;
        private BigDecimal moyenneGenerale;
        private Integer creditsAcquis;
        private Integer creditsTotal;
        private Boolean semestreValide;
        private Boolean publie;
        private LocalDateTime datePublication;
        private List<UeDetailDTO> detailUes;

        public BulletinDTOBuilder id(Long v) { this.id = v; return this; }
        public BulletinDTOBuilder etudiantNom(String v) { this.etudiantNom = v; return this; }
        public BulletinDTOBuilder semestreLabel(String v) { this.semestreLabel = v; return this; }
        public BulletinDTOBuilder moyenneGenerale(BigDecimal v) { this.moyenneGenerale = v; return this; }
        public BulletinDTOBuilder creditsAcquis(Integer v) { this.creditsAcquis = v; return this; }
        public BulletinDTOBuilder creditsTotal(Integer v) { this.creditsTotal = v; return this; }
        public BulletinDTOBuilder semestreValide(Boolean v) { this.semestreValide = v; return this; }
        public BulletinDTOBuilder publie(Boolean v) { this.publie = v; return this; }
        public BulletinDTOBuilder datePublication(LocalDateTime v) { this.datePublication = v; return this; }
        public BulletinDTOBuilder detailUes(List<UeDetailDTO> v) { this.detailUes = v; return this; }

        public BulletinDTO build() {
            BulletinDTO obj = new BulletinDTO();
            if (this.id != null) obj.id = this.id;
            if (this.etudiantNom != null) obj.etudiantNom = this.etudiantNom;
            if (this.semestreLabel != null) obj.semestreLabel = this.semestreLabel;
            if (this.moyenneGenerale != null) obj.moyenneGenerale = this.moyenneGenerale;
            if (this.creditsAcquis != null) obj.creditsAcquis = this.creditsAcquis;
            if (this.creditsTotal != null) obj.creditsTotal = this.creditsTotal;
            if (this.semestreValide != null) obj.semestreValide = this.semestreValide;
            if (this.publie != null) obj.publie = this.publie;
            if (this.datePublication != null) obj.datePublication = this.datePublication;
            if (this.detailUes != null) obj.detailUes = this.detailUes;
            return obj;
        }
    }
}

 public static class UeDetailDTO {

    private String ue;
    private int credits;
    private Double moyenneUe;
    private List<MatiereDetailDTO> matieres;

    // getters & setters
    public String getUe() { return this.ue; }
    public void setUe(String ue) { this.ue = ue; }
    public int getCredits() { return this.credits; }
    public void setCredits(int credits) { this.credits = credits; }
    public Double getMoyenneUe() { return this.moyenneUe; }
    public void setMoyenneUe(Double moyenneUe) { this.moyenneUe = moyenneUe; }
    public List<MatiereDetailDTO> getMatieres() { return this.matieres; }
    public void setMatieres(List<MatiereDetailDTO> matieres) { this.matieres = matieres; }

    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static UeDetailDTOBuilder builder() { return new UeDetailDTOBuilder(); }

    public static class UeDetailDTOBuilder {
        private String ue;
        private Integer credits;
        private Double moyenneUe;
        private List<MatiereDetailDTO> matieres;

        public UeDetailDTOBuilder ue(String v) { this.ue = v; return this; }
        public UeDetailDTOBuilder credits(Integer v) { this.credits = v; return this; }
        public UeDetailDTOBuilder moyenneUe(Double v) { this.moyenneUe = v; return this; }
        public UeDetailDTOBuilder matieres(List<MatiereDetailDTO> v) { this.matieres = v; return this; }

        public UeDetailDTO build() {
            UeDetailDTO obj = new UeDetailDTO();
            if (this.ue != null) obj.ue = this.ue;
            if (this.credits != null) obj.credits = this.credits;
            if (this.moyenneUe != null) obj.moyenneUe = this.moyenneUe;
            if (this.matieres != null) obj.matieres = this.matieres;
            return obj;
        }
    }
}

 public static class MatiereDetailDTO {

    private String matiere;
    private double coefficient;
    private Double moyenne;

    // getters & setters
    public String getMatiere() { return this.matiere; }
    public void setMatiere(String matiere) { this.matiere = matiere; }
    public double getCoefficient() { return this.coefficient; }
    public void setCoefficient(double coefficient) { this.coefficient = coefficient; }
    public Double getMoyenne() { return this.moyenne; }
    public void setMoyenne(Double moyenne) { this.moyenne = moyenne; }

    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static MatiereDetailDTOBuilder builder() { return new MatiereDetailDTOBuilder(); }

    public static class MatiereDetailDTOBuilder {
        private String matiere;
        private Double coefficient;
        private Double moyenne;

        public MatiereDetailDTOBuilder matiere(String v) { this.matiere = v; return this; }
        public MatiereDetailDTOBuilder coefficient(Double v) { this.coefficient = v; return this; }
        public MatiereDetailDTOBuilder moyenne(Double v) { this.moyenne = v; return this; }

        public MatiereDetailDTO build() {
            MatiereDetailDTO obj = new MatiereDetailDTO();
            if (this.matiere != null) obj.matiere = this.matiere;
            if (this.coefficient != null) obj.coefficient = this.coefficient;
            if (this.moyenne != null) obj.moyenne = this.moyenne;
            return obj;
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// PROGRESSION ÉTUDIANT (EF-11)
// ENF-2 : uniquement les données de l'étudiant connecté
// ══════════════════════════════════════════════════════════════════════════

 public static class ProgressionItemDTO {

    private Long quizId;
    private String quizTitre;
    private String typeQuiz;
    private LocalDateTime date;
    private Double noteSur20; // null si quiz pas clôturé

    // getters & setters
    public Long getQuizId() { return this.quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }
    public String getQuizTitre() { return this.quizTitre; }
    public void setQuizTitre(String quizTitre) { this.quizTitre = quizTitre; }
    public String getTypeQuiz() { return this.typeQuiz; }
    public void setTypeQuiz(String typeQuiz) { this.typeQuiz = typeQuiz; }
    public LocalDateTime getDate() { return this.date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public Double getNoteSur20() { return this.noteSur20; }
    public void setNoteSur20(Double noteSur20) { this.noteSur20 = noteSur20; }

    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static ProgressionItemDTOBuilder builder() { return new ProgressionItemDTOBuilder(); }

    public static class ProgressionItemDTOBuilder {
        private Long quizId;
        private String quizTitre;
        private String typeQuiz;
        private LocalDateTime date;
        private Double noteSur20;

        public ProgressionItemDTOBuilder quizId(Long v) { this.quizId = v; return this; }
        public ProgressionItemDTOBuilder quizTitre(String v) { this.quizTitre = v; return this; }
        public ProgressionItemDTOBuilder typeQuiz(String v) { this.typeQuiz = v; return this; }
        public ProgressionItemDTOBuilder date(LocalDateTime v) { this.date = v; return this; }
        public ProgressionItemDTOBuilder noteSur20(Double v) { this.noteSur20 = v; return this; }

        public ProgressionItemDTO build() {
            ProgressionItemDTO obj = new ProgressionItemDTO();
            if (this.quizId != null) obj.quizId = this.quizId;
            if (this.quizTitre != null) obj.quizTitre = this.quizTitre;
            if (this.typeQuiz != null) obj.typeQuiz = this.typeQuiz;
            if (this.date != null) obj.date = this.date;
            if (this.noteSur20 != null) obj.noteSur20 = this.noteSur20;
            return obj;
        }
    }
}

 public static class ProgressionMatiereDTO {

    private String matiere;
    private String etudiant; // ENF-2 : son propre nom uniquement
    private List<ProgressionItemDTO> historique;

    // getters & setters
    public String getMatiere() { return this.matiere; }
    public void setMatiere(String matiere) { this.matiere = matiere; }
    public String getEtudiant() { return this.etudiant; }
    public void setEtudiant(String etudiant) { this.etudiant = etudiant; }
    public List<ProgressionItemDTO> getHistorique() { return this.historique; }
    public void setHistorique(List<ProgressionItemDTO> historique) { this.historique = historique; }

    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static ProgressionMatiereDTOBuilder builder() { return new ProgressionMatiereDTOBuilder(); }

    public static class ProgressionMatiereDTOBuilder {
        private String matiere;
        private String etudiant;
        private List<ProgressionItemDTO> historique;

        public ProgressionMatiereDTOBuilder matiere(String v) { this.matiere = v; return this; }
        public ProgressionMatiereDTOBuilder etudiant(String v) { this.etudiant = v; return this; }
        public ProgressionMatiereDTOBuilder historique(List<ProgressionItemDTO> v) { this.historique = v; return this; }

        public ProgressionMatiereDTO build() {
            ProgressionMatiereDTO obj = new ProgressionMatiereDTO();
            if (this.matiere != null) obj.matiere = this.matiere;
            if (this.etudiant != null) obj.etudiant = this.etudiant;
            if (this.historique != null) obj.historique = this.historique;
            return obj;
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// RÉFÉRENTIEL ACADÉMIQUE (v3) — Filière, Niveau, Année, Classe, Semestre, UE, Matière
// ══════════════════════════════════════════════════════════════════════════

 public static class FiliereDTO {

    private Long id;
    private String nom;
    private String code;

    // getters & setters
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return this.nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getCode() { return this.code; }
    public void setCode(String code) { this.code = code; }

    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static FiliereDTOBuilder builder() { return new FiliereDTOBuilder(); }

    public static class FiliereDTOBuilder {
        private Long id;
        private String nom;
        private String code;

        public FiliereDTOBuilder id(Long v) { this.id = v; return this; }
        public FiliereDTOBuilder nom(String v) { this.nom = v; return this; }
        public FiliereDTOBuilder code(String v) { this.code = v; return this; }

        public FiliereDTO build() {
            FiliereDTO obj = new FiliereDTO();
            if (this.id != null) obj.id = this.id;
            if (this.nom != null) obj.nom = this.nom;
            if (this.code != null) obj.code = this.code;
            return obj;
        }
    }
}
 public static class CreateFiliereRequest {

    @NotBlank private String nom;
    @NotBlank private String code;

    // getters & setters
    public String getNom() { return this.nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getCode() { return this.code; }
    public void setCode(String code) { this.code = code; }
}

 public static class NiveauDTO {

    private Long id;
    private String libelle;
    private String filiereNom;

    // getters & setters
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getLibelle() { return this.libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public String getFiliereNom() { return this.filiereNom; }
    public void setFiliereNom(String filiereNom) { this.filiereNom = filiereNom; }

    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static NiveauDTOBuilder builder() { return new NiveauDTOBuilder(); }

    public static class NiveauDTOBuilder {
        private Long id;
        private String libelle;
        private String filiereNom;

        public NiveauDTOBuilder id(Long v) { this.id = v; return this; }
        public NiveauDTOBuilder libelle(String v) { this.libelle = v; return this; }
        public NiveauDTOBuilder filiereNom(String v) { this.filiereNom = v; return this; }

        public NiveauDTO build() {
            NiveauDTO obj = new NiveauDTO();
            if (this.id != null) obj.id = this.id;
            if (this.libelle != null) obj.libelle = this.libelle;
            if (this.filiereNom != null) obj.filiereNom = this.filiereNom;
            return obj;
        }
    }
}
 public static class CreateNiveauRequest {

    @NotBlank private String libelle;
    @NotNull private Long filiereId;

    // getters & setters
    public String getLibelle() { return this.libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public Long getFiliereId() { return this.filiereId; }
    public void setFiliereId(Long filiereId) { this.filiereId = filiereId; }
}

 public static class AnneeAcademiqueDTO {

    private Long id;
    private String libelle;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private boolean active;

    // getters & setters
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getLibelle() { return this.libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public LocalDate getDateDebut() { return this.dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public LocalDate getDateFin() { return this.dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
    public boolean isActive() { return this.active; }
    public void setActive(boolean active) { this.active = active; }

    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static AnneeAcademiqueDTOBuilder builder() { return new AnneeAcademiqueDTOBuilder(); }

    public static class AnneeAcademiqueDTOBuilder {
        private Long id;
        private String libelle;
        private LocalDate dateDebut;
        private LocalDate dateFin;
        private Boolean active;

        public AnneeAcademiqueDTOBuilder id(Long v) { this.id = v; return this; }
        public AnneeAcademiqueDTOBuilder libelle(String v) { this.libelle = v; return this; }
        public AnneeAcademiqueDTOBuilder dateDebut(LocalDate v) { this.dateDebut = v; return this; }
        public AnneeAcademiqueDTOBuilder dateFin(LocalDate v) { this.dateFin = v; return this; }
        public AnneeAcademiqueDTOBuilder active(Boolean v) { this.active = v; return this; }

        public AnneeAcademiqueDTO build() {
            AnneeAcademiqueDTO obj = new AnneeAcademiqueDTO();
            if (this.id != null) obj.id = this.id;
            if (this.libelle != null) obj.libelle = this.libelle;
            if (this.dateDebut != null) obj.dateDebut = this.dateDebut;
            if (this.dateFin != null) obj.dateFin = this.dateFin;
            if (this.active != null) obj.active = this.active;
            return obj;
        }
    }
}
 public static class CreateAnneeRequest {

    @NotBlank private String libelle;
    @NotNull private LocalDate dateDebut;
    @NotNull private LocalDate dateFin;

    // getters & setters
    public String getLibelle() { return this.libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public LocalDate getDateDebut() { return this.dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public LocalDate getDateFin() { return this.dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
}

 public static class ClasseDTO {

    private Long id;
    private String nom;
    private String niveauLibelle;
    private String anneeLibelle;

    // getters & setters
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return this.nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getNiveauLibelle() { return this.niveauLibelle; }
    public void setNiveauLibelle(String niveauLibelle) { this.niveauLibelle = niveauLibelle; }
    public String getAnneeLibelle() { return this.anneeLibelle; }
    public void setAnneeLibelle(String anneeLibelle) { this.anneeLibelle = anneeLibelle; }

    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static ClasseDTOBuilder builder() { return new ClasseDTOBuilder(); }

    public static class ClasseDTOBuilder {
        private Long id;
        private String nom;
        private String niveauLibelle;
        private String anneeLibelle;

        public ClasseDTOBuilder id(Long v) { this.id = v; return this; }
        public ClasseDTOBuilder nom(String v) { this.nom = v; return this; }
        public ClasseDTOBuilder niveauLibelle(String v) { this.niveauLibelle = v; return this; }
        public ClasseDTOBuilder anneeLibelle(String v) { this.anneeLibelle = v; return this; }

        public ClasseDTO build() {
            ClasseDTO obj = new ClasseDTO();
            if (this.id != null) obj.id = this.id;
            if (this.nom != null) obj.nom = this.nom;
            if (this.niveauLibelle != null) obj.niveauLibelle = this.niveauLibelle;
            if (this.anneeLibelle != null) obj.anneeLibelle = this.anneeLibelle;
            return obj;
        }
    }
}
 public static class CreateClasseRequest {

    @NotBlank private String nom;
    @NotNull private Long niveauId;
    @NotNull private Long anneeAcademiqueId;

    // getters & setters
    public String getNom() { return this.nom; }
    public void setNom(String nom) { this.nom = nom; }
    public Long getNiveauId() { return this.niveauId; }
    public void setNiveauId(Long niveauId) { this.niveauId = niveauId; }
    public Long getAnneeAcademiqueId() { return this.anneeAcademiqueId; }
    public void setAnneeAcademiqueId(Long anneeAcademiqueId) { this.anneeAcademiqueId = anneeAcademiqueId; }
}

 public static class SemestreDTO {

    private Long id;
    private Integer numero;
    private String niveauLibelle;
    private String anneeLibelle;

    // getters & setters
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public Integer getNumero() { return this.numero; }
    public void setNumero(Integer numero) { this.numero = numero; }
    public String getNiveauLibelle() { return this.niveauLibelle; }
    public void setNiveauLibelle(String niveauLibelle) { this.niveauLibelle = niveauLibelle; }
    public String getAnneeLibelle() { return this.anneeLibelle; }
    public void setAnneeLibelle(String anneeLibelle) { this.anneeLibelle = anneeLibelle; }

    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static SemestreDTOBuilder builder() { return new SemestreDTOBuilder(); }

    public static class SemestreDTOBuilder {
        private Long id;
        private Integer numero;
        private String niveauLibelle;
        private String anneeLibelle;

        public SemestreDTOBuilder id(Long v) { this.id = v; return this; }
        public SemestreDTOBuilder numero(Integer v) { this.numero = v; return this; }
        public SemestreDTOBuilder niveauLibelle(String v) { this.niveauLibelle = v; return this; }
        public SemestreDTOBuilder anneeLibelle(String v) { this.anneeLibelle = v; return this; }

        public SemestreDTO build() {
            SemestreDTO obj = new SemestreDTO();
            if (this.id != null) obj.id = this.id;
            if (this.numero != null) obj.numero = this.numero;
            if (this.niveauLibelle != null) obj.niveauLibelle = this.niveauLibelle;
            if (this.anneeLibelle != null) obj.anneeLibelle = this.anneeLibelle;
            return obj;
        }
    }
}
 public static class CreateSemestreRequest {

    @NotNull @Min(1) @Max(2) private Integer numero;
    @NotNull private Long niveauId;
    @NotNull private Long anneeAcademiqueId;

    // getters & setters
    public Integer getNumero() { return this.numero; }
    public void setNumero(Integer numero) { this.numero = numero; }
    public Long getNiveauId() { return this.niveauId; }
    public void setNiveauId(Long niveauId) { this.niveauId = niveauId; }
    public Long getAnneeAcademiqueId() { return this.anneeAcademiqueId; }
    public void setAnneeAcademiqueId(Long anneeAcademiqueId) { this.anneeAcademiqueId = anneeAcademiqueId; }
}

 public static class UeDTO {

    private Long id;
    private String nom;
    private Integer credits;
    private BigDecimal seuilValidation;
    private String semestreLabel;

    // getters & setters
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return this.nom; }
    public void setNom(String nom) { this.nom = nom; }
    public Integer getCredits() { return this.credits; }
    public void setCredits(Integer credits) { this.credits = credits; }
    public BigDecimal getSeuilValidation() { return this.seuilValidation; }
    public void setSeuilValidation(BigDecimal seuilValidation) { this.seuilValidation = seuilValidation; }
    public String getSemestreLabel() { return this.semestreLabel; }
    public void setSemestreLabel(String semestreLabel) { this.semestreLabel = semestreLabel; }

    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static UeDTOBuilder builder() { return new UeDTOBuilder(); }

    public static class UeDTOBuilder {
        private Long id;
        private String nom;
        private Integer credits;
        private BigDecimal seuilValidation;
        private String semestreLabel;

        public UeDTOBuilder id(Long v) { this.id = v; return this; }
        public UeDTOBuilder nom(String v) { this.nom = v; return this; }
        public UeDTOBuilder credits(Integer v) { this.credits = v; return this; }
        public UeDTOBuilder seuilValidation(BigDecimal v) { this.seuilValidation = v; return this; }
        public UeDTOBuilder semestreLabel(String v) { this.semestreLabel = v; return this; }

        public UeDTO build() {
            UeDTO obj = new UeDTO();
            if (this.id != null) obj.id = this.id;
            if (this.nom != null) obj.nom = this.nom;
            if (this.credits != null) obj.credits = this.credits;
            if (this.seuilValidation != null) obj.seuilValidation = this.seuilValidation;
            if (this.semestreLabel != null) obj.semestreLabel = this.semestreLabel;
            return obj;
        }
    }
}
 public static class CreateUeRequest {

    @NotBlank private String nom;
    @NotNull @Min(1) private Integer credits;
    @DecimalMin("0") @DecimalMax("20") private BigDecimal seuilValidation = BigDecimal.TEN;
    @NotNull private Long semestreId;

    // getters & setters
    public String getNom() { return this.nom; }
    public void setNom(String nom) { this.nom = nom; }
    public Integer getCredits() { return this.credits; }
    public void setCredits(Integer credits) { this.credits = credits; }
    public BigDecimal getSeuilValidation() { return this.seuilValidation; }
    public void setSeuilValidation(BigDecimal seuilValidation) { this.seuilValidation = seuilValidation; }
    public Long getSemestreId() { return this.semestreId; }
    public void setSemestreId(Long semestreId) { this.semestreId = semestreId; }
}

 public static class MatiereDTO {

    private Long id;
    private String nom;
    private BigDecimal coefficient;
    private String ueNom;
    private List<String> enseignants;

    // getters & setters
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return this.nom; }
    public void setNom(String nom) { this.nom = nom; }
    public BigDecimal getCoefficient() { return this.coefficient; }
    public void setCoefficient(BigDecimal coefficient) { this.coefficient = coefficient; }
    public String getUeNom() { return this.ueNom; }
    public void setUeNom(String ueNom) { this.ueNom = ueNom; }
    public List<String> getEnseignants() { return this.enseignants; }
    public void setEnseignants(List<String> enseignants) { this.enseignants = enseignants; }

    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static MatiereDTOBuilder builder() { return new MatiereDTOBuilder(); }

    public static class MatiereDTOBuilder {
        private Long id;
        private String nom;
        private BigDecimal coefficient;
        private String ueNom;
        private List<String> enseignants;

        public MatiereDTOBuilder id(Long v) { this.id = v; return this; }
        public MatiereDTOBuilder nom(String v) { this.nom = v; return this; }
        public MatiereDTOBuilder coefficient(BigDecimal v) { this.coefficient = v; return this; }
        public MatiereDTOBuilder ueNom(String v) { this.ueNom = v; return this; }
        public MatiereDTOBuilder enseignants(List<String> v) { this.enseignants = v; return this; }

        public MatiereDTO build() {
            MatiereDTO obj = new MatiereDTO();
            if (this.id != null) obj.id = this.id;
            if (this.nom != null) obj.nom = this.nom;
            if (this.coefficient != null) obj.coefficient = this.coefficient;
            if (this.ueNom != null) obj.ueNom = this.ueNom;
            if (this.enseignants != null) obj.enseignants = this.enseignants;
            return obj;
        }
    }
}
 public static class CreateMatiereRequest {

    @NotBlank private String nom;
    @DecimalMin("0.1") private BigDecimal coefficient = BigDecimal.ONE;
    @NotNull private Long ueId;

    // getters & setters
    public String getNom() { return this.nom; }
    public void setNom(String nom) { this.nom = nom; }
    public BigDecimal getCoefficient() { return this.coefficient; }
    public void setCoefficient(BigDecimal coefficient) { this.coefficient = coefficient; }
    public Long getUeId() { return this.ueId; }
    public void setUeId(Long ueId) { this.ueId = ueId; }
}
 public static class AffecterEnseignantRequest {

    @NotNull private Long enseignantId;
    @NotNull private Long anneeAcademiqueId;

    // getters & setters
    public Long getEnseignantId() { return this.enseignantId; }
    public void setEnseignantId(Long enseignantId) { this.enseignantId = enseignantId; }
    public Long getAnneeAcademiqueId() { return this.anneeAcademiqueId; }
    public void setAnneeAcademiqueId(Long anneeAcademiqueId) { this.anneeAcademiqueId = anneeAcademiqueId; }
}

 public static class InscrireEtudiantRequest {

    @NotNull private Long etudiantId;
    @NotNull private Long classeId;
    @NotNull private Long anneeAcademiqueId;

    // getters & setters
    public Long getEtudiantId() { return this.etudiantId; }
    public void setEtudiantId(Long etudiantId) { this.etudiantId = etudiantId; }
    public Long getClasseId() { return this.classeId; }
    public void setClasseId(Long classeId) { this.classeId = classeId; }
    public Long getAnneeAcademiqueId() { return this.anneeAcademiqueId; }
    public void setAnneeAcademiqueId(Long anneeAcademiqueId) { this.anneeAcademiqueId = anneeAcademiqueId; }
}

// ══════════════════════════════════════════════════════════════════════════
// NOTIFICATIONS
// ══════════════════════════════════════════════════════════════════════════

 public static class NotificationDTO {

    private Long id;
    private String type;
    private String titre;
    private String message;
    private boolean lue;
    private LocalDateTime createdAt;
    private Long referenceId;

    // getters & setters
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return this.type; }
    public void setType(String type) { this.type = type; }
    public String getTitre() { return this.titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getMessage() { return this.message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isLue() { return this.lue; }
    public void setLue(boolean lue) { this.lue = lue; }
    public LocalDateTime getCreatedAt() { return this.createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getReferenceId() { return this.referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }

    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static NotificationDTOBuilder builder() { return new NotificationDTOBuilder(); }

    public static class NotificationDTOBuilder {
        private Long id;
        private String type;
        private String titre;
        private String message;
        private Boolean lue;
        private LocalDateTime createdAt;
        private Long referenceId;

        public NotificationDTOBuilder id(Long v) { this.id = v; return this; }
        public NotificationDTOBuilder type(String v) { this.type = v; return this; }
        public NotificationDTOBuilder titre(String v) { this.titre = v; return this; }
        public NotificationDTOBuilder message(String v) { this.message = v; return this; }
        public NotificationDTOBuilder lue(Boolean v) { this.lue = v; return this; }
        public NotificationDTOBuilder createdAt(LocalDateTime v) { this.createdAt = v; return this; }
        public NotificationDTOBuilder referenceId(Long v) { this.referenceId = v; return this; }

        public NotificationDTO build() {
            NotificationDTO obj = new NotificationDTO();
            if (this.id != null) obj.id = this.id;
            if (this.type != null) obj.type = this.type;
            if (this.titre != null) obj.titre = this.titre;
            if (this.message != null) obj.message = this.message;
            if (this.lue != null) obj.lue = this.lue;
            if (this.createdAt != null) obj.createdAt = this.createdAt;
            if (this.referenceId != null) obj.referenceId = this.referenceId;
            return obj;
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// BANQUE DE QUESTIONS — CRUD complet (v3)
// ══════════════════════════════════════════════════════════════════════════

 public static class QuestionFullDTO {

    private Long id;
    private String enonce;
    private String type;
    private String difficulte;
    private double points;
    private String explication;
    private String matiereNom;
    private List<ChoixCorrigeDTO> choix; // avec estCorrect : vue enseignant uniquement
    private List<String> tags = List.of(); // v3.4 : étiquettes thématiques (chapitre, thème...)

    // getters & setters
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getEnonce() { return this.enonce; }
    public void setEnonce(String enonce) { this.enonce = enonce; }
    public String getType() { return this.type; }
    public void setType(String type) { this.type = type; }
    public String getDifficulte() { return this.difficulte; }
    public void setDifficulte(String difficulte) { this.difficulte = difficulte; }
    public double getPoints() { return this.points; }
    public void setPoints(double points) { this.points = points; }
    public String getExplication() { return this.explication; }
    public void setExplication(String explication) { this.explication = explication; }
    public String getMatiereNom() { return this.matiereNom; }
    public void setMatiereNom(String matiereNom) { this.matiereNom = matiereNom; }
    public List<ChoixCorrigeDTO> getChoix() { return this.choix; }
    public void setChoix(List<ChoixCorrigeDTO> choix) { this.choix = choix; }
    public List<String> getTags() { return this.tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static QuestionFullDTOBuilder builder() { return new QuestionFullDTOBuilder(); }

    public static class QuestionFullDTOBuilder {
        private Long id;
        private String enonce;
        private String type;
        private String difficulte;
        private Double points;
        private String explication;
        private String matiereNom;
        private List<ChoixCorrigeDTO> choix;
        private List<String> tags;

        public QuestionFullDTOBuilder id(Long v) { this.id = v; return this; }
        public QuestionFullDTOBuilder enonce(String v) { this.enonce = v; return this; }
        public QuestionFullDTOBuilder type(String v) { this.type = v; return this; }
        public QuestionFullDTOBuilder difficulte(String v) { this.difficulte = v; return this; }
        public QuestionFullDTOBuilder points(Double v) { this.points = v; return this; }
        public QuestionFullDTOBuilder explication(String v) { this.explication = v; return this; }
        public QuestionFullDTOBuilder matiereNom(String v) { this.matiereNom = v; return this; }
        public QuestionFullDTOBuilder choix(List<ChoixCorrigeDTO> v) { this.choix = v; return this; }
        public QuestionFullDTOBuilder tags(List<String> v) { this.tags = v; return this; }

        public QuestionFullDTO build() {
            QuestionFullDTO obj = new QuestionFullDTO();
            if (this.id != null) obj.id = this.id;
            if (this.enonce != null) obj.enonce = this.enonce;
            if (this.type != null) obj.type = this.type;
            if (this.difficulte != null) obj.difficulte = this.difficulte;
            if (this.points != null) obj.points = this.points;
            if (this.explication != null) obj.explication = this.explication;
            if (this.matiereNom != null) obj.matiereNom = this.matiereNom;
            if (this.choix != null) obj.choix = this.choix;
            if (this.tags != null) obj.tags = this.tags;
            return obj;
        }
    }
}

 public static class SaveQuestionRequest {

    @NotBlank private String enonce;
    @NotBlank private String type;       // QCM_UNIQUE, QCM_MULTIPLE, VRAI_FAUX, REPONSE_COURTE
    private String difficulte = "MOYEN"; // FACILE, MOYEN, DIFFICILE
    @Min(0) private double points = 1.0;
    private String explication;
    @NotNull private Long matiereId;
    private List<CreateChoixRequest> choix;
    private List<String> tags; // v3.4 : libellés d'étiquettes ; créées à la volée si nouvelles

    // getters & setters
    public String getEnonce() { return this.enonce; }
    public void setEnonce(String enonce) { this.enonce = enonce; }
    public String getType() { return this.type; }
    public void setType(String type) { this.type = type; }
    public String getDifficulte() { return this.difficulte; }
    public void setDifficulte(String difficulte) { this.difficulte = difficulte; }
    public double getPoints() { return this.points; }
    public void setPoints(double points) { this.points = points; }
    public String getExplication() { return this.explication; }
    public void setExplication(String explication) { this.explication = explication; }
    public Long getMatiereId() { return this.matiereId; }
    public void setMatiereId(Long matiereId) { this.matiereId = matiereId; }
    public List<CreateChoixRequest> getChoix() { return this.choix; }
    public void setChoix(List<CreateChoixRequest> choix) { this.choix = choix; }
    public List<String> getTags() { return this.tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}

// ── Inscription libre (demande de compte) ─────────────────────────────────
 public static class InscriptionLibreRequest {

    @NotBlank private String username;
    @NotBlank private String password;
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    private String email;
    @NotBlank private String role; // ETUDIANT ou ENSEIGNANT uniquement
    private Long classeId;         // obligatoire si role = ETUDIANT
    private String motif;          // pourquoi je m'inscris (optionnel)

    // getters & setters
    public String getUsername() { return this.username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return this.password; }
    public void setPassword(String password) { this.password = password; }
    public String getFirstName() { return this.firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return this.lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return this.email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return this.role; }
    public void setRole(String role) { this.role = role; }
    public Long getClasseId() { return this.classeId; }
    public void setClasseId(Long classeId) { this.classeId = classeId; }
    public String getMotif() { return this.motif; }
    public void setMotif(String motif) { this.motif = motif; }
}

 public static class DemandeCompteDTO {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String statutCompte;
    private String classeNom;
    private String motif;

    // getters & setters
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return this.username; }
    public void setUsername(String username) { this.username = username; }
    public String getFirstName() { return this.firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return this.lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return this.email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return this.role; }
    public void setRole(String role) { this.role = role; }
    public String getStatutCompte() { return this.statutCompte; }
    public void setStatutCompte(String statutCompte) { this.statutCompte = statutCompte; }
    public String getClasseNom() { return this.classeNom; }
    public void setClasseNom(String classeNom) { this.classeNom = classeNom; }
    public String getMotif() { return this.motif; }
    public void setMotif(String motif) { this.motif = motif; }
}

// ══════════════════════════════════════════════════════════════════════════
// STATISTIQUES ADMIN
// ══════════════════════════════════════════════════════════════════════════

public static class EffectifClasseDTO {
    private String classeNom;
    private long nbEtudiants;

    public String getClasseNom() { return this.classeNom; }
    public void setClasseNom(String classeNom) { this.classeNom = classeNom; }
    public long getNbEtudiants() { return this.nbEtudiants; }
    public void setNbEtudiants(long nbEtudiants) { this.nbEtudiants = nbEtudiants; }

    public static EffectifClasseDTOBuilder builder() { return new EffectifClasseDTOBuilder(); }
    public static class EffectifClasseDTOBuilder {
        private String classeNom;
        private long nbEtudiants;
        public EffectifClasseDTOBuilder classeNom(String v) { this.classeNom = v; return this; }
        public EffectifClasseDTOBuilder nbEtudiants(long v) { this.nbEtudiants = v; return this; }
        public EffectifClasseDTO build() {
            EffectifClasseDTO obj = new EffectifClasseDTO();
            obj.classeNom = this.classeNom;
            obj.nbEtudiants = this.nbEtudiants;
            return obj;
        }
    }
}

public static class MoyenneMatiereDTO {
    private String matiereNom;
    private double moyenne;
    private long nbNotes;

    public String getMatiereNom() { return this.matiereNom; }
    public void setMatiereNom(String matiereNom) { this.matiereNom = matiereNom; }
    public double getMoyenne() { return this.moyenne; }
    public void setMoyenne(double moyenne) { this.moyenne = moyenne; }
    public long getNbNotes() { return this.nbNotes; }
    public void setNbNotes(long nbNotes) { this.nbNotes = nbNotes; }

    public static MoyenneMatiereDTOBuilder builder() { return new MoyenneMatiereDTOBuilder(); }
    public static class MoyenneMatiereDTOBuilder {
        private String matiereNom;
        private double moyenne;
        private long nbNotes;
        public MoyenneMatiereDTOBuilder matiereNom(String v) { this.matiereNom = v; return this; }
        public MoyenneMatiereDTOBuilder moyenne(double v) { this.moyenne = v; return this; }
        public MoyenneMatiereDTOBuilder nbNotes(long v) { this.nbNotes = v; return this; }
        public MoyenneMatiereDTO build() {
            MoyenneMatiereDTO obj = new MoyenneMatiereDTO();
            obj.matiereNom = this.matiereNom;
            obj.moyenne = this.moyenne;
            obj.nbNotes = this.nbNotes;
            return obj;
        }
    }
}

public static class ActiviteEnseignantDTO {
    private String enseignantNom;
    private long nbDevoirs;

    public String getEnseignantNom() { return this.enseignantNom; }
    public void setEnseignantNom(String enseignantNom) { this.enseignantNom = enseignantNom; }
    public long getNbDevoirs() { return this.nbDevoirs; }
    public void setNbDevoirs(long nbDevoirs) { this.nbDevoirs = nbDevoirs; }

    public static ActiviteEnseignantDTOBuilder builder() { return new ActiviteEnseignantDTOBuilder(); }
    public static class ActiviteEnseignantDTOBuilder {
        private String enseignantNom;
        private long nbDevoirs;
        public ActiviteEnseignantDTOBuilder enseignantNom(String v) { this.enseignantNom = v; return this; }
        public ActiviteEnseignantDTOBuilder nbDevoirs(long v) { this.nbDevoirs = v; return this; }
        public ActiviteEnseignantDTO build() {
            ActiviteEnseignantDTO obj = new ActiviteEnseignantDTO();
            obj.enseignantNom = this.enseignantNom;
            obj.nbDevoirs = this.nbDevoirs;
            return obj;
        }
    }
}

public static class AdminStatsDTO {
    private long nbEtudiants;
    private long nbEnseignants;
    private long nbClasses;
    private long nbDevoirs;
    private double moyenneGenerale;
    private List<EffectifClasseDTO> effectifsParClasse;
    private List<MoyenneMatiereDTO> moyennesParMatiere;
    private List<ActiviteEnseignantDTO> activiteEnseignants;

    public long getNbEtudiants() { return this.nbEtudiants; }
    public void setNbEtudiants(long nbEtudiants) { this.nbEtudiants = nbEtudiants; }
    public long getNbEnseignants() { return this.nbEnseignants; }
    public void setNbEnseignants(long nbEnseignants) { this.nbEnseignants = nbEnseignants; }
    public long getNbClasses() { return this.nbClasses; }
    public void setNbClasses(long nbClasses) { this.nbClasses = nbClasses; }
    public long getNbDevoirs() { return this.nbDevoirs; }
    public void setNbDevoirs(long nbDevoirs) { this.nbDevoirs = nbDevoirs; }
    public double getMoyenneGenerale() { return this.moyenneGenerale; }
    public void setMoyenneGenerale(double moyenneGenerale) { this.moyenneGenerale = moyenneGenerale; }
    public List<EffectifClasseDTO> getEffectifsParClasse() { return this.effectifsParClasse; }
    public void setEffectifsParClasse(List<EffectifClasseDTO> v) { this.effectifsParClasse = v; }
    public List<MoyenneMatiereDTO> getMoyennesParMatiere() { return this.moyennesParMatiere; }
    public void setMoyennesParMatiere(List<MoyenneMatiereDTO> v) { this.moyennesParMatiere = v; }
    public List<ActiviteEnseignantDTO> getActiviteEnseignants() { return this.activiteEnseignants; }
    public void setActiviteEnseignants(List<ActiviteEnseignantDTO> v) { this.activiteEnseignants = v; }

    public static AdminStatsDTOBuilder builder() { return new AdminStatsDTOBuilder(); }
    public static class AdminStatsDTOBuilder {
        private long nbEtudiants;
        private long nbEnseignants;
        private long nbClasses;
        private long nbDevoirs;
        private double moyenneGenerale;
        private List<EffectifClasseDTO> effectifsParClasse;
        private List<MoyenneMatiereDTO> moyennesParMatiere;
        private List<ActiviteEnseignantDTO> activiteEnseignants;

        public AdminStatsDTOBuilder nbEtudiants(long v) { this.nbEtudiants = v; return this; }
        public AdminStatsDTOBuilder nbEnseignants(long v) { this.nbEnseignants = v; return this; }
        public AdminStatsDTOBuilder nbClasses(long v) { this.nbClasses = v; return this; }
        public AdminStatsDTOBuilder nbDevoirs(long v) { this.nbDevoirs = v; return this; }
        public AdminStatsDTOBuilder moyenneGenerale(double v) { this.moyenneGenerale = v; return this; }
        public AdminStatsDTOBuilder effectifsParClasse(List<EffectifClasseDTO> v) { this.effectifsParClasse = v; return this; }
        public AdminStatsDTOBuilder moyennesParMatiere(List<MoyenneMatiereDTO> v) { this.moyennesParMatiere = v; return this; }
        public AdminStatsDTOBuilder activiteEnseignants(List<ActiviteEnseignantDTO> v) { this.activiteEnseignants = v; return this; }

        public AdminStatsDTO build() {
            AdminStatsDTO obj = new AdminStatsDTO();
            obj.nbEtudiants = this.nbEtudiants;
            obj.nbEnseignants = this.nbEnseignants;
            obj.nbClasses = this.nbClasses;
            obj.nbDevoirs = this.nbDevoirs;
            obj.moyenneGenerale = this.moyenneGenerale;
            obj.effectifsParClasse = this.effectifsParClasse;
            obj.moyennesParMatiere = this.moyennesParMatiere;
            obj.activiteEnseignants = this.activiteEnseignants;
            return obj;
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// INSCRIPTIONS (admin) — rattachement d'un étudiant existant à une classe
// pour une année académique donnée.
// ══════════════════════════════════════════════════════════════════════════

public static class CreateInscriptionRequest {
    @NotNull private Long etudiantId;
    @NotNull private Long classeId;
    @NotNull private Long anneeAcademiqueId;

    public Long getEtudiantId() { return this.etudiantId; }
    public void setEtudiantId(Long v) { this.etudiantId = v; }
    public Long getClasseId() { return this.classeId; }
    public void setClasseId(Long v) { this.classeId = v; }
    public Long getAnneeAcademiqueId() { return this.anneeAcademiqueId; }
    public void setAnneeAcademiqueId(Long v) { this.anneeAcademiqueId = v; }
}

public static class InscriptionDTO {
    private Long id;
    private String etudiantNom;
    private String classeNom;
    private String anneeLibelle;
    private String statut;
    private LocalDateTime dateInscription;

    public Long getId() { return this.id; }
    public void setId(Long v) { this.id = v; }
    public String getEtudiantNom() { return this.etudiantNom; }
    public void setEtudiantNom(String v) { this.etudiantNom = v; }
    public String getClasseNom() { return this.classeNom; }
    public void setClasseNom(String v) { this.classeNom = v; }
    public String getAnneeLibelle() { return this.anneeLibelle; }
    public void setAnneeLibelle(String v) { this.anneeLibelle = v; }
    public String getStatut() { return this.statut; }
    public void setStatut(String v) { this.statut = v; }
    public LocalDateTime getDateInscription() { return this.dateInscription; }
    public void setDateInscription(LocalDateTime v) { this.dateInscription = v; }

    public static InscriptionDTOBuilder builder() { return new InscriptionDTOBuilder(); }

    public static class InscriptionDTOBuilder {
        private Long id;
        private String etudiantNom;
        private String classeNom;
        private String anneeLibelle;
        private String statut;
        private LocalDateTime dateInscription;

        public InscriptionDTOBuilder id(Long v) { this.id = v; return this; }
        public InscriptionDTOBuilder etudiantNom(String v) { this.etudiantNom = v; return this; }
        public InscriptionDTOBuilder classeNom(String v) { this.classeNom = v; return this; }
        public InscriptionDTOBuilder anneeLibelle(String v) { this.anneeLibelle = v; return this; }
        public InscriptionDTOBuilder statut(String v) { this.statut = v; return this; }
        public InscriptionDTOBuilder dateInscription(LocalDateTime v) { this.dateInscription = v; return this; }

        public InscriptionDTO build() {
            InscriptionDTO obj = new InscriptionDTO();
            obj.id = this.id;
            obj.etudiantNom = this.etudiantNom;
            obj.classeNom = this.classeNom;
            obj.anneeLibelle = this.anneeLibelle;
            obj.statut = this.statut;
            obj.dateInscription = this.dateInscription;
            return obj;
        }
    }
}
}


