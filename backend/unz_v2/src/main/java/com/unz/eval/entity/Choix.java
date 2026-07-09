package com.unz.eval.entity;

import jakarta.persistence.*;

/**
 * Choix de réponse d'une question.
 * ENF-1 : estCorrect n'est JAMAIS exposé dans les DTO publics avant clôture.
 */
@Entity
@Table(name = "choix")
public class Choix {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String texte;

    /**
     * ENF-1 : ce champ ne doit JAMAIS apparaître dans les réponses API
     * avant la clôture officielle du quiz.
     * Voir : QuestionPublicDTO (pas de estCorrect) vs QuestionCorrigeDTO (avec estCorrect)
     */
    @Column(nullable = false)
    
    private boolean estCorrect = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    // ── Getters & Setters ──
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getTexte() { return this.texte; }
    public void setTexte(String texte) { this.texte = texte; }
    public boolean isEstCorrect() { return this.estCorrect; }
    public void setEstCorrect(boolean estCorrect) { this.estCorrect = estCorrect; }
    public Question getQuestion() { return this.question; }
    public void setQuestion(Question question) { this.question = question; }
    public Choix() {}


    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static ChoixBuilder builder() { return new ChoixBuilder(); }

    public static class ChoixBuilder {
        private Long id;
        private String texte;
        private Boolean estCorrect;
        private Question question;

        public ChoixBuilder id(Long v) { this.id = v; return this; }
        public ChoixBuilder texte(String v) { this.texte = v; return this; }
        public ChoixBuilder estCorrect(Boolean v) { this.estCorrect = v; return this; }
        public ChoixBuilder question(Question v) { this.question = v; return this; }

        public Choix build() {
            Choix obj = new Choix();
            if (this.id != null) obj.id = this.id;
            if (this.texte != null) obj.texte = this.texte;
            if (this.estCorrect != null) obj.estCorrect = this.estCorrect;
            if (this.question != null) obj.question = this.question;
            return obj;
        }
    }
}
