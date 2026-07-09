package com.unz.eval.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "matieres")
public class Matiere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false, precision = 4, scale = 2)
    
    private BigDecimal coefficient = BigDecimal.ONE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ue_id", nullable = false)
    private UE ue;

    /** v3 : remplace l'ancien lien direct Matiere.enseignant (1-1) */
    @OneToMany(mappedBy = "matiere", cascade = CascadeType.ALL)
    private List<AffectationEnseignant> affectations;

    @OneToMany(mappedBy = "matiere", cascade = CascadeType.ALL)
    private List<Quiz> quizzes;

    @OneToMany(mappedBy = "matiere", cascade = CascadeType.ALL)
    private List<Question> questions;

    /** Enseignant(s) affectés à cette matière pour une année académique donnée */
    public List<User> getEnseignantsPour(AnneeAcademique annee) {
        if (affectations == null) return List.of();
        return affectations.stream()
                .filter(a -> a.getAnneeAcademique().getId().equals(annee.getId()))
                .map(AffectationEnseignant::getEnseignant)
                .toList();
    }

    /** Vérifie si un utilisateur donné enseigne cette matière (toutes années confondues) */
    public boolean estEnseignePar(User user) {
        if (affectations == null) return false;
        return affectations.stream().anyMatch(a -> a.getEnseignant().getId().equals(user.getId()));
    }
    // ── Getters & Setters ──
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return this.nom; }
    public void setNom(String nom) { this.nom = nom; }
    public BigDecimal getCoefficient() { return this.coefficient; }
    public void setCoefficient(BigDecimal coefficient) { this.coefficient = coefficient; }
    public UE getUe() { return this.ue; }
    public void setUe(UE ue) { this.ue = ue; }
    public List<AffectationEnseignant> getAffectations() { return this.affectations; }
    public void setAffectations(List<AffectationEnseignant> affectations) { this.affectations = affectations; }
    public List<Quiz> getQuizzes() { return this.quizzes; }
    public void setQuizzes(List<Quiz> quizzes) { this.quizzes = quizzes; }
    public List<Question> getQuestions() { return this.questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
    public Matiere() {}


    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static MatiereBuilder builder() { return new MatiereBuilder(); }

    public static class MatiereBuilder {
        private Long id;
        private String nom;
        private BigDecimal coefficient;
        private UE ue;
        private List<AffectationEnseignant> affectations;
        private List<Quiz> quizzes;
        private List<Question> questions;

        public MatiereBuilder id(Long v) { this.id = v; return this; }
        public MatiereBuilder nom(String v) { this.nom = v; return this; }
        public MatiereBuilder coefficient(BigDecimal v) { this.coefficient = v; return this; }
        public MatiereBuilder ue(UE v) { this.ue = v; return this; }
        public MatiereBuilder affectations(List<AffectationEnseignant> v) { this.affectations = v; return this; }
        public MatiereBuilder quizzes(List<Quiz> v) { this.quizzes = v; return this; }
        public MatiereBuilder questions(List<Question> v) { this.questions = v; return this; }

        public Matiere build() {
            Matiere obj = new Matiere();
            if (this.id != null) obj.id = this.id;
            if (this.nom != null) obj.nom = this.nom;
            if (this.coefficient != null) obj.coefficient = this.coefficient;
            if (this.ue != null) obj.ue = this.ue;
            if (this.affectations != null) obj.affectations = this.affectations;
            if (this.quizzes != null) obj.quizzes = this.quizzes;
            if (this.questions != null) obj.questions = this.questions;
            return obj;
        }
    }
}
