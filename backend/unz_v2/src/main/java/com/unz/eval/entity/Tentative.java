package com.unz.eval.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Tentative de passage d'un quiz par un étudiant.
 * EF-5  : une seule tentative pour EXAMEN, plusieurs pour ENTRAINEMENT
 * EF-9  : correction automatique à la soumission
 * EF-10 : note masquée jusqu'à clôture validée par l'enseignant
 * ENF-2 : un étudiant ne voit que ses propres tentatives
 * v3    : feedback enseignant + traçabilité des événements suspects (TentativeEvenement)
 */
@Entity
@Table(name = "tentatives")
public class Tentative {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    /** ENF-2 : clé de cloisonnement — toujours filtré sur etudiant = utilisateur connecté */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private User etudiant;

    @Column(nullable = false)
    
    private LocalDateTime dateDebut = LocalDateTime.now();

    private LocalDateTime dateSoumission;

    /** EF-9 : note calculée automatiquement à la soumission */
    @Column(precision = 6, scale = 2)
    private BigDecimal noteObtenue;

    /** v3 : retour libre de l'enseignant, utile notamment pour les questions REPONSE_COURTE */
    @Column(name = "feedback_enseignant", columnDefinition = "TEXT")
    private String feedbackEnseignant;

    @OneToMany(mappedBy = "tentative", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reponse> reponses;

    /** v3 : événements anti-triche relevés pendant la tentative */
    @OneToMany(mappedBy = "tentative", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TentativeEvenement> evenements;

    // ── Correction automatique (EF-9) ────────────────────────────────────
    public void corriger() {
        if (reponses == null) { noteObtenue = BigDecimal.ZERO; return; }
        double total = 0;
        for (Reponse r : reponses) {
            if (r.estCorrecte()) {
                total += pointsPour(r.getQuestion());
            }
        }
        noteObtenue = BigDecimal.valueOf(total);
        dateSoumission = LocalDateTime.now();
    }

    /** Points effectifs d'une question dans ce quiz (respecte l'éventuel pointsOverride) */
    private double pointsPour(Question question) {
        if (quiz.getQuizQuestions() == null) return question.getPoints();
        return quiz.getQuizQuestions().stream()
                .filter(qq -> qq.getQuestion().getId().equals(question.getId()))
                .findFirst()
                .map(QuizQuestion::getPointsEffectifs)
                .orElse(question.getPoints());
    }

    public double getNoteObtenueSur20() {
        double bareme = quiz.getBaremeTotal();
        if (bareme == 0 || noteObtenue == null) return 0;
        return noteObtenue.doubleValue() / bareme * 20;
    }

    /** v3.1 : score exprimé sur le barème déclaré par l'enseignant (20, 40, 50…) */
    public double getNoteObtenueSurNoteSur() {
        double bareme = quiz.getBaremeTotal();
        if (bareme == 0 || noteObtenue == null) return 0;
        return noteObtenue.doubleValue() / bareme * quiz.getNoteSur().doubleValue();
    }
    // ── Getters & Setters ──
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public Quiz getQuiz() { return this.quiz; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }
    public User getEtudiant() { return this.etudiant; }
    public void setEtudiant(User etudiant) { this.etudiant = etudiant; }
    public LocalDateTime getDateDebut() { return this.dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }
    public LocalDateTime getDateSoumission() { return this.dateSoumission; }
    public void setDateSoumission(LocalDateTime dateSoumission) { this.dateSoumission = dateSoumission; }
    public BigDecimal getNoteObtenue() { return this.noteObtenue; }
    public void setNoteObtenue(BigDecimal noteObtenue) { this.noteObtenue = noteObtenue; }
    public String getFeedbackEnseignant() { return this.feedbackEnseignant; }
    public void setFeedbackEnseignant(String feedbackEnseignant) { this.feedbackEnseignant = feedbackEnseignant; }
    public List<Reponse> getReponses() { return this.reponses; }
    public void setReponses(List<Reponse> reponses) { this.reponses = reponses; }
    public List<TentativeEvenement> getEvenements() { return this.evenements; }
    public void setEvenements(List<TentativeEvenement> evenements) { this.evenements = evenements; }
    public Tentative() {}


    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static TentativeBuilder builder() { return new TentativeBuilder(); }

    public static class TentativeBuilder {
        private Long id;
        private Quiz quiz;
        private User etudiant;
        private LocalDateTime dateDebut;
        private LocalDateTime dateSoumission;
        private BigDecimal noteObtenue;
        private String feedbackEnseignant;
        private List<Reponse> reponses;
        private List<TentativeEvenement> evenements;

        public TentativeBuilder id(Long v) { this.id = v; return this; }
        public TentativeBuilder quiz(Quiz v) { this.quiz = v; return this; }
        public TentativeBuilder etudiant(User v) { this.etudiant = v; return this; }
        public TentativeBuilder dateDebut(LocalDateTime v) { this.dateDebut = v; return this; }
        public TentativeBuilder dateSoumission(LocalDateTime v) { this.dateSoumission = v; return this; }
        public TentativeBuilder noteObtenue(BigDecimal v) { this.noteObtenue = v; return this; }
        public TentativeBuilder feedbackEnseignant(String v) { this.feedbackEnseignant = v; return this; }
        public TentativeBuilder reponses(List<Reponse> v) { this.reponses = v; return this; }
        public TentativeBuilder evenements(List<TentativeEvenement> v) { this.evenements = v; return this; }

        public Tentative build() {
            Tentative obj = new Tentative();
            if (this.id != null) obj.id = this.id;
            if (this.quiz != null) obj.quiz = this.quiz;
            if (this.etudiant != null) obj.etudiant = this.etudiant;
            if (this.dateDebut != null) obj.dateDebut = this.dateDebut;
            if (this.dateSoumission != null) obj.dateSoumission = this.dateSoumission;
            if (this.noteObtenue != null) obj.noteObtenue = this.noteObtenue;
            if (this.feedbackEnseignant != null) obj.feedbackEnseignant = this.feedbackEnseignant;
            if (this.reponses != null) obj.reponses = this.reponses;
            if (this.evenements != null) obj.evenements = this.evenements;
            return obj;
        }
    }
}
