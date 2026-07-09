package com.unz.eval.entity;

import jakarta.persistence.*;

/**
 * Association Quiz ↔ Question avec un ordre de passage.
 * v3 : ajout d'un barème spécifique au quiz (pointsOverride), qui prime sur
 * Question.points quand il est renseigné — une même question peut valoir
 * un nombre de points différent selon le quiz dans lequel elle est utilisée.
 */
@Entity
@Table(name = "quiz_questions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"quiz_id", "question_id"}))
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false)
    
    private Integer ordre = 0;

    @Column(name = "points_override")
    private Double pointsOverride;

    public double getPointsEffectifs() {
        return pointsOverride != null ? pointsOverride : question.getPoints();
    }
    // ── Getters & Setters ──
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public Quiz getQuiz() { return this.quiz; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }
    public Question getQuestion() { return this.question; }
    public void setQuestion(Question question) { this.question = question; }
    public Integer getOrdre() { return this.ordre; }
    public void setOrdre(Integer ordre) { this.ordre = ordre; }
    public Double getPointsOverride() { return this.pointsOverride; }
    public void setPointsOverride(Double pointsOverride) { this.pointsOverride = pointsOverride; }
    public QuizQuestion() {}


    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static QuizQuestionBuilder builder() { return new QuizQuestionBuilder(); }

    public static class QuizQuestionBuilder {
        private Long id;
        private Quiz quiz;
        private Question question;
        private Integer ordre;
        private Double pointsOverride;

        public QuizQuestionBuilder id(Long v) { this.id = v; return this; }
        public QuizQuestionBuilder quiz(Quiz v) { this.quiz = v; return this; }
        public QuizQuestionBuilder question(Question v) { this.question = v; return this; }
        public QuizQuestionBuilder ordre(Integer v) { this.ordre = v; return this; }
        public QuizQuestionBuilder pointsOverride(Double v) { this.pointsOverride = v; return this; }

        public QuizQuestion build() {
            QuizQuestion obj = new QuizQuestion();
            if (this.id != null) obj.id = this.id;
            if (this.quiz != null) obj.quiz = this.quiz;
            if (this.question != null) obj.question = this.question;
            if (this.ordre != null) obj.ordre = this.ordre;
            if (this.pointsOverride != null) obj.pointsOverride = this.pointsOverride;
            return obj;
        }
    }
}
