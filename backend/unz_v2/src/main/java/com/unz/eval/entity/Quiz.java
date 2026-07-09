package com.unz.eval.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Quiz / évaluation.
 * EF-3  : rattaché à une matière
 * EF-4  : QCM réponse unique ou multiple
 * EF-5  : type EXAMEN ou ENTRAINEMENT
 * EF-6  : durée chronométrée
 * EF-8  : barème par question
 * ENF-1 : les bonnes réponses ne sont jamais exposées avant clôture
 */
@Entity
@Table(name = "quizzes")
public class Quiz {

    public enum TypeQuiz { EXAMEN, ENTRAINEMENT }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false)
    
    private TypeQuiz typeQuiz = TypeQuiz.EXAMEN;

    /**
     * v3.1 : barème déclaré du devoir — noté sur 20, 40, 50... au choix de l'enseignant.
     * La correction interne reste basée sur la somme réelle des points par question
     * (getBaremeTotal()) ; noteSur est la cible affichée et sert à convertir le score
     * obtenu à l'échelle annoncée aux étudiants (voir Tentative.getNoteObtenueSurNoteSur()).
     */
    @Column(name = "note_sur", nullable = false, precision = 6, scale = 2)
    
    private java.math.BigDecimal noteSur = java.math.BigDecimal.valueOf(20);

    /** EF-6 : durée en minutes */
    @Column(nullable = false)
    
    private Integer dureeMinutes = 30;

    /** EF-16 : nombre max de tentatives (Entraînement) */
    @Column(nullable = false)
    
    private Integer tentativesMax = 1;

    /** EF-16 : délai minimum entre deux tentatives (en minutes) */
    @Column(nullable = false)
    
    private Integer delaiEntreTentativesMinutes = 0;

    private LocalDateTime dateOuverture;
    private LocalDateTime dateCloture;

    /**
     * EF-10 : flag explicite de clôture validée par l'enseignant.
     * Tant que false, les notes restent invisibles pour les étudiants.
     */
    @Column(nullable = false)
    
    private boolean clotureValideeParEnseignant = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matiere_id", nullable = false)
    private Matiere matiere;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_id", nullable = false)
    private Classe classe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cree_par_id", nullable = false)
    private User creePar;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordre ASC")
    private List<QuizQuestion> quizQuestions;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL)
    private List<Tentative> tentatives;

    // ── Helpers ──────────────────────────────────────────────────────────

    public boolean isEstCloture() {
        if (clotureValideeParEnseignant) return true;
        return dateCloture != null && LocalDateTime.now().isAfter(dateCloture);
    }

    public boolean isNotesVisibles() {
        return clotureValideeParEnseignant;
    }

    /** EF-8 : total des points du quiz (respecte l'éventuel pointsOverride de chaque QuizQuestion) */
    public double getBaremeTotal() {
        if (quizQuestions == null) return 0;
        return quizQuestions.stream()
                .mapToDouble(QuizQuestion::getPointsEffectifs)
                .sum();
    }
    // ── Getters & Setters ──
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getTitre() { return this.titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public TypeQuiz getTypeQuiz() { return this.typeQuiz; }
    public void setTypeQuiz(TypeQuiz typeQuiz) { this.typeQuiz = typeQuiz; }
    public Integer getDureeMinutes() { return this.dureeMinutes; }
    public void setDureeMinutes(Integer dureeMinutes) { this.dureeMinutes = dureeMinutes; }
    public Integer getTentativesMax() { return this.tentativesMax; }
    public void setTentativesMax(Integer tentativesMax) { this.tentativesMax = tentativesMax; }
    public Integer getDelaiEntreTentativesMinutes() { return this.delaiEntreTentativesMinutes; }
    public void setDelaiEntreTentativesMinutes(Integer delaiEntreTentativesMinutes) { this.delaiEntreTentativesMinutes = delaiEntreTentativesMinutes; }
    public LocalDateTime getDateOuverture() { return this.dateOuverture; }
    public void setDateOuverture(LocalDateTime dateOuverture) { this.dateOuverture = dateOuverture; }
    public LocalDateTime getDateCloture() { return this.dateCloture; }
    public void setDateCloture(LocalDateTime dateCloture) { this.dateCloture = dateCloture; }
    public boolean isClotureValideeParEnseignant() { return this.clotureValideeParEnseignant; }
    public void setClotureValideeParEnseignant(boolean clotureValideeParEnseignant) { this.clotureValideeParEnseignant = clotureValideeParEnseignant; }
    public Matiere getMatiere() { return this.matiere; }
    public void setMatiere(Matiere matiere) { this.matiere = matiere; }
    public Classe getClasse() { return this.classe; }
    public void setClasse(Classe classe) { this.classe = classe; }
    public User getCreePar() { return this.creePar; }
    public void setCreePar(User creePar) { this.creePar = creePar; }
    public List<QuizQuestion> getQuizQuestions() { return this.quizQuestions; }
    public void setQuizQuestions(List<QuizQuestion> quizQuestions) { this.quizQuestions = quizQuestions; }
    public List<Tentative> getTentatives() { return this.tentatives; }
    public void setTentatives(List<Tentative> tentatives) { this.tentatives = tentatives; }
    public java.math.BigDecimal getNoteSur() { return this.noteSur; }
    public void setNoteSur(java.math.BigDecimal noteSur) { this.noteSur = noteSur; }
    public Quiz() {}


    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static QuizBuilder builder() { return new QuizBuilder(); }

    public static class QuizBuilder {
        private Long id;
        private String titre;
        private TypeQuiz typeQuiz;
        private java.math.BigDecimal noteSur;
        private Integer dureeMinutes;
        private Integer tentativesMax;
        private Integer delaiEntreTentativesMinutes;
        private LocalDateTime dateOuverture;
        private LocalDateTime dateCloture;
        private Boolean clotureValideeParEnseignant;
        private Matiere matiere;
        private Classe classe;
        private User creePar;
        private List<QuizQuestion> quizQuestions;
        private List<Tentative> tentatives;

        public QuizBuilder id(Long v) { this.id = v; return this; }
        public QuizBuilder titre(String v) { this.titre = v; return this; }
        public QuizBuilder typeQuiz(TypeQuiz v) { this.typeQuiz = v; return this; }
        public QuizBuilder noteSur(java.math.BigDecimal v) { this.noteSur = v; return this; }
        public QuizBuilder dureeMinutes(Integer v) { this.dureeMinutes = v; return this; }
        public QuizBuilder tentativesMax(Integer v) { this.tentativesMax = v; return this; }
        public QuizBuilder delaiEntreTentativesMinutes(Integer v) { this.delaiEntreTentativesMinutes = v; return this; }
        public QuizBuilder dateOuverture(LocalDateTime v) { this.dateOuverture = v; return this; }
        public QuizBuilder dateCloture(LocalDateTime v) { this.dateCloture = v; return this; }
        public QuizBuilder clotureValideeParEnseignant(Boolean v) { this.clotureValideeParEnseignant = v; return this; }
        public QuizBuilder matiere(Matiere v) { this.matiere = v; return this; }
        public QuizBuilder classe(Classe v) { this.classe = v; return this; }
        public QuizBuilder creePar(User v) { this.creePar = v; return this; }
        public QuizBuilder quizQuestions(List<QuizQuestion> v) { this.quizQuestions = v; return this; }
        public QuizBuilder tentatives(List<Tentative> v) { this.tentatives = v; return this; }

        public Quiz build() {
            Quiz obj = new Quiz();
            if (this.id != null) obj.id = this.id;
            if (this.titre != null) obj.titre = this.titre;
            if (this.typeQuiz != null) obj.typeQuiz = this.typeQuiz;
            if (this.noteSur != null) obj.noteSur = this.noteSur;
            if (this.dureeMinutes != null) obj.dureeMinutes = this.dureeMinutes;
            if (this.tentativesMax != null) obj.tentativesMax = this.tentativesMax;
            if (this.delaiEntreTentativesMinutes != null) obj.delaiEntreTentativesMinutes = this.delaiEntreTentativesMinutes;
            if (this.dateOuverture != null) obj.dateOuverture = this.dateOuverture;
            if (this.dateCloture != null) obj.dateCloture = this.dateCloture;
            if (this.clotureValideeParEnseignant != null) obj.clotureValideeParEnseignant = this.clotureValideeParEnseignant;
            if (this.matiere != null) obj.matiere = this.matiere;
            if (this.classe != null) obj.classe = this.classe;
            if (this.creePar != null) obj.creePar = this.creePar;
            if (this.quizQuestions != null) obj.quizQuestions = this.quizQuestions;
            if (this.tentatives != null) obj.tentatives = this.tentatives;
            return obj;
        }
    }
}
