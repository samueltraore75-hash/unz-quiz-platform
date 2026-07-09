package com.unz.eval.entity;

import jakarta.persistence.*;

/**
 * Affectation d'un enseignant à une matière pour une année académique donnée.
 * NOUVEAU (v3) : remplace le lien simple Matiere.enseignant (1-1), permet la
 * co-intervention et conserve l'historique d'affectation d'une année sur l'autre.
 */
@Entity
@Table(name = "affectations_enseignants",
       uniqueConstraints = @UniqueConstraint(columnNames = {"enseignant_id", "matiere_id", "annee_academique_id"}))
public class AffectationEnseignant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enseignant_id", nullable = false)
    private User enseignant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matiere_id", nullable = false)
    private Matiere matiere;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annee_academique_id", nullable = false)
    private AnneeAcademique anneeAcademique;
    // ── Getters & Setters ──
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public User getEnseignant() { return this.enseignant; }
    public void setEnseignant(User enseignant) { this.enseignant = enseignant; }
    public Matiere getMatiere() { return this.matiere; }
    public void setMatiere(Matiere matiere) { this.matiere = matiere; }
    public AnneeAcademique getAnneeAcademique() { return this.anneeAcademique; }
    public void setAnneeAcademique(AnneeAcademique anneeAcademique) { this.anneeAcademique = anneeAcademique; }
    public AffectationEnseignant() {}


    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static AffectationEnseignantBuilder builder() { return new AffectationEnseignantBuilder(); }

    public static class AffectationEnseignantBuilder {
        private Long id;
        private User enseignant;
        private Matiere matiere;
        private AnneeAcademique anneeAcademique;

        public AffectationEnseignantBuilder id(Long v) { this.id = v; return this; }
        public AffectationEnseignantBuilder enseignant(User v) { this.enseignant = v; return this; }
        public AffectationEnseignantBuilder matiere(Matiere v) { this.matiere = v; return this; }
        public AffectationEnseignantBuilder anneeAcademique(AnneeAcademique v) { this.anneeAcademique = v; return this; }

        public AffectationEnseignant build() {
            AffectationEnseignant obj = new AffectationEnseignant();
            if (this.id != null) obj.id = this.id;
            if (this.enseignant != null) obj.enseignant = this.enseignant;
            if (this.matiere != null) obj.matiere = this.matiere;
            if (this.anneeAcademique != null) obj.anneeAcademique = this.anneeAcademique;
            return obj;
        }
    }
}
