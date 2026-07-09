package com.unz.eval.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "classes", uniqueConstraints = @UniqueConstraint(columnNames = {"nom", "annee_academique_id"}))
public class Classe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @ManyToOne(optional = false)
    @JoinColumn(name = "niveau_id", nullable = false)
    private Niveau niveau;

    /** v3 : remplace l'ancien champ texte "anneeAcademique" */
    @ManyToOne(optional = false)
    @JoinColumn(name = "annee_academique_id", nullable = false)
    private AnneeAcademique anneeAcademique;

    /** v3 : les étudiants d'une classe passent par Inscription, plus par un lien direct */
    @OneToMany(mappedBy = "classe", fetch = FetchType.LAZY)
    private List<Inscription> inscriptions;

    @Override
    public String toString() { return nom + " (" + anneeAcademique.getLibelle() + ")"; }
    // ── Getters & Setters ──
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return this.nom; }
    public void setNom(String nom) { this.nom = nom; }
    public Niveau getNiveau() { return this.niveau; }
    public void setNiveau(Niveau niveau) { this.niveau = niveau; }
    public AnneeAcademique getAnneeAcademique() { return this.anneeAcademique; }
    public void setAnneeAcademique(AnneeAcademique anneeAcademique) { this.anneeAcademique = anneeAcademique; }
    public List<Inscription> getInscriptions() { return this.inscriptions; }
    public void setInscriptions(List<Inscription> inscriptions) { this.inscriptions = inscriptions; }
    public Classe() {}


    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static ClasseBuilder builder() { return new ClasseBuilder(); }

    public static class ClasseBuilder {
        private Long id;
        private String nom;
        private Niveau niveau;
        private AnneeAcademique anneeAcademique;
        private List<Inscription> inscriptions;

        public ClasseBuilder id(Long v) { this.id = v; return this; }
        public ClasseBuilder nom(String v) { this.nom = v; return this; }
        public ClasseBuilder niveau(Niveau v) { this.niveau = v; return this; }
        public ClasseBuilder anneeAcademique(AnneeAcademique v) { this.anneeAcademique = v; return this; }
        public ClasseBuilder inscriptions(List<Inscription> v) { this.inscriptions = v; return this; }

        public Classe build() {
            Classe obj = new Classe();
            if (this.id != null) obj.id = this.id;
            if (this.nom != null) obj.nom = this.nom;
            if (this.niveau != null) obj.niveau = this.niveau;
            if (this.anneeAcademique != null) obj.anneeAcademique = this.anneeAcademique;
            if (this.inscriptions != null) obj.inscriptions = this.inscriptions;
            return obj;
        }
    }
}
