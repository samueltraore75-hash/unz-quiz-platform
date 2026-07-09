package com.unz.eval.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Réponse d'un étudiant à une question dans une tentative.
 * v3 : ajout de reponseTexte pour les questions de type REPONSE_COURTE
 * (pas de correction automatique pour ce type — notation manuelle par l'enseignant).
 */
@Entity
@Table(name = "reponses",
       uniqueConstraints = @UniqueConstraint(columnNames = {"tentative_id", "question_id"}))
public class Reponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tentative_id", nullable = false)
    private Tentative tentative;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    /** Choix sélectionnés par l'étudiant (QCM_UNIQUE / QCM_MULTIPLE / VRAI_FAUX) */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "reponse_choix",
        joinColumns = @JoinColumn(name = "reponse_id"),
        inverseJoinColumns = @JoinColumn(name = "choix_id")
    )
    private Set<Choix> choixSelectionnes;

    /** v3 : réponse libre pour les questions REPONSE_COURTE */
    @Column(name = "reponse_texte", length = 500)
    private String reponseTexte;

    /**
     * EF-9 : vérifie si la réponse est exactement correcte.
     * REPONSE_COURTE renvoie toujours false ici : elle nécessite une correction
     * manuelle par l'enseignant, hors périmètre de la correction automatique.
     */
    public boolean estCorrecte() {
        if (question.getType() == Question.TypeQuestion.REPONSE_COURTE) return false;
        if (choixSelectionnes == null || question.getChoix() == null) return false;
        Set<Long> selectionnes = new HashSet<>();
        choixSelectionnes.forEach(c -> selectionnes.add(c.getId()));
        Set<Long> corrects = new HashSet<>();
        question.getChoix().stream()
                .filter(Choix::isEstCorrect)
                .forEach(c -> corrects.add(c.getId()));
        return selectionnes.equals(corrects);
    }
    // ── Getters & Setters ──
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public Tentative getTentative() { return this.tentative; }
    public void setTentative(Tentative tentative) { this.tentative = tentative; }
    public Question getQuestion() { return this.question; }
    public void setQuestion(Question question) { this.question = question; }
    public Set<Choix> getChoixSelectionnes() { return this.choixSelectionnes; }
    public void setChoixSelectionnes(Set<Choix> choixSelectionnes) { this.choixSelectionnes = choixSelectionnes; }
    public String getReponseTexte() { return this.reponseTexte; }
    public void setReponseTexte(String reponseTexte) { this.reponseTexte = reponseTexte; }
    public Reponse() {}


    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static ReponseBuilder builder() { return new ReponseBuilder(); }

    public static class ReponseBuilder {
        private Long id;
        private Tentative tentative;
        private Question question;
        private Set<Choix> choixSelectionnes;
        private String reponseTexte;

        public ReponseBuilder id(Long v) { this.id = v; return this; }
        public ReponseBuilder tentative(Tentative v) { this.tentative = v; return this; }
        public ReponseBuilder question(Question v) { this.question = v; return this; }
        public ReponseBuilder choixSelectionnes(Set<Choix> v) { this.choixSelectionnes = v; return this; }
        public ReponseBuilder reponseTexte(String v) { this.reponseTexte = v; return this; }

        public Reponse build() {
            Reponse obj = new Reponse();
            if (this.id != null) obj.id = this.id;
            if (this.tentative != null) obj.tentative = this.tentative;
            if (this.question != null) obj.question = this.question;
            if (this.choixSelectionnes != null) obj.choixSelectionnes = this.choixSelectionnes;
            if (this.reponseTexte != null) obj.reponseTexte = this.reponseTexte;
            return obj;
        }
    }
}
