package com.unz.eval.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;

import java.util.List;

/**
 * Question réutilisable dans la banque.
 * EF-7  : banque de questions réutilisable entre plusieurs quiz.
 * v3    : type étendu (QCM unique/multiple, Vrai/Faux, réponse courte), difficulté, tags, pièces jointes.
 */
@Entity
@Table(name = "questions")
public class Question {

    public enum TypeQuestion { QCM_UNIQUE, QCM_MULTIPLE, VRAI_FAUX, REPONSE_COURTE }
    public enum Difficulte { FACILE, MOYEN, DIFFICILE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String enonce;

    /** v3.2 : explication pédagogique affichée à l'étudiant après correction (facultative) */
    @Column(columnDefinition = "TEXT")
    private String explication;

    /** v3 : remplace le booléen reponseMultiple, ouvre la porte à Vrai/Faux et réponse courte */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false)
    
    private TypeQuestion type = TypeQuestion.QCM_UNIQUE;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false)
    
    private Difficulte difficulte = Difficulte.MOYEN;

    /** EF-8 : points associés à cette question (peut être surchargé par quiz, voir QuizQuestion) */
    @Column(nullable = false)
    
    private double points = 1.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matiere_id", nullable = false)
    private Matiere matiere;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Choix> choix;

    @ManyToMany
    @JoinTable(name = "question_tags",
        joinColumns = @JoinColumn(name = "question_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<Tag> tags;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PieceJointe> piecesJointes;

    public boolean isReponseMultiple() { return type == TypeQuestion.QCM_MULTIPLE; }

    /** REPONSE_COURTE n'a pas de correction automatique : notation manuelle par l'enseignant */
    public boolean isCorrectionManuelle() { return type == TypeQuestion.REPONSE_COURTE; }
    // ── Getters & Setters ──
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getEnonce() { return this.enonce; }
    public void setEnonce(String enonce) { this.enonce = enonce; }
    public String getExplication() { return this.explication; }
    public void setExplication(String explication) { this.explication = explication; }
    public TypeQuestion getType() { return this.type; }
    public void setType(TypeQuestion type) { this.type = type; }
    public Difficulte getDifficulte() { return this.difficulte; }
    public void setDifficulte(Difficulte difficulte) { this.difficulte = difficulte; }
    public double getPoints() { return this.points; }
    public void setPoints(double points) { this.points = points; }
    public Matiere getMatiere() { return this.matiere; }
    public void setMatiere(Matiere matiere) { this.matiere = matiere; }
    public List<Choix> getChoix() { return this.choix; }
    public void setChoix(List<Choix> choix) { this.choix = choix; }
    public List<Tag> getTags() { return this.tags; }
    public void setTags(List<Tag> tags) { this.tags = tags; }
    public List<PieceJointe> getPiecesJointes() { return this.piecesJointes; }
    public void setPiecesJointes(List<PieceJointe> piecesJointes) { this.piecesJointes = piecesJointes; }
    public Question() {}


    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static QuestionBuilder builder() { return new QuestionBuilder(); }

    public static class QuestionBuilder {
        private Long id;
        private String enonce;
        private String explication;
        private TypeQuestion type;
        private Difficulte difficulte;
        private Double points;
        private Matiere matiere;
        private List<Choix> choix;
        private List<Tag> tags;
        private List<PieceJointe> piecesJointes;

        public QuestionBuilder id(Long v) { this.id = v; return this; }
        public QuestionBuilder enonce(String v) { this.enonce = v; return this; }
        public QuestionBuilder explication(String v) { this.explication = v; return this; }
        public QuestionBuilder type(TypeQuestion v) { this.type = v; return this; }
        public QuestionBuilder difficulte(Difficulte v) { this.difficulte = v; return this; }
        public QuestionBuilder points(Double v) { this.points = v; return this; }
        public QuestionBuilder matiere(Matiere v) { this.matiere = v; return this; }
        public QuestionBuilder choix(List<Choix> v) { this.choix = v; return this; }
        public QuestionBuilder tags(List<Tag> v) { this.tags = v; return this; }
        public QuestionBuilder piecesJointes(List<PieceJointe> v) { this.piecesJointes = v; return this; }

        public Question build() {
            Question obj = new Question();
            if (this.id != null) obj.id = this.id;
            if (this.enonce != null) obj.enonce = this.enonce;
            if (this.explication != null) obj.explication = this.explication;
            if (this.type != null) obj.type = this.type;
            if (this.difficulte != null) obj.difficulte = this.difficulte;
            if (this.points != null) obj.points = this.points;
            if (this.matiere != null) obj.matiere = this.matiere;
            if (this.choix != null) obj.choix = this.choix;
            if (this.tags != null) obj.tags = this.tags;
            if (this.piecesJointes != null) obj.piecesJointes = this.piecesJointes;
            return obj;
        }
    }
}
