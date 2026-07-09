package com.unz.eval.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Année académique (ex : "2025-2026").
 * NOUVEAU (v3) : remplace le champ texte dupliqué sur Classe et Semestre.
 * Permet d'ouvrir/clôturer une année et de rattacher Inscription et AffectationEnseignant.
 */
@Entity
@Table(name = "annees_academiques")
public class AnneeAcademique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String libelle;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(nullable = false)
    
    private boolean active = false;

    @Override
    public String toString() { return libelle; }
    // ── Getters & Setters ──
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
    public AnneeAcademique() {}


    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static AnneeAcademiqueBuilder builder() { return new AnneeAcademiqueBuilder(); }

    public static class AnneeAcademiqueBuilder {
        private Long id;
        private String libelle;
        private LocalDate dateDebut;
        private LocalDate dateFin;
        private Boolean active;

        public AnneeAcademiqueBuilder id(Long v) { this.id = v; return this; }
        public AnneeAcademiqueBuilder libelle(String v) { this.libelle = v; return this; }
        public AnneeAcademiqueBuilder dateDebut(LocalDate v) { this.dateDebut = v; return this; }
        public AnneeAcademiqueBuilder dateFin(LocalDate v) { this.dateFin = v; return this; }
        public AnneeAcademiqueBuilder active(Boolean v) { this.active = v; return this; }

        public AnneeAcademique build() {
            AnneeAcademique obj = new AnneeAcademique();
            if (this.id != null) obj.id = this.id;
            if (this.libelle != null) obj.libelle = this.libelle;
            if (this.dateDebut != null) obj.dateDebut = this.dateDebut;
            if (this.dateFin != null) obj.dateFin = this.dateFin;
            if (this.active != null) obj.active = this.active;
            return obj;
        }
    }
}
