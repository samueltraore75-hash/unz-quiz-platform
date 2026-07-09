package com.unz.eval.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Rattachement annuel d'un étudiant à une classe.
 * NOUVEAU (v3) : remplace l'ancien lien fixe User.classe. Une Inscription par
 * (étudiant, année académique) permet le suivi de progression L1 → L2 → L3
 * sans perdre l'historique des classes précédentes (§1.2 du cahier des charges).
 */
@Entity
@Table(name = "inscriptions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"etudiant_id", "annee_academique_id"}))
public class Inscription {

    public enum Statut { EN_COURS, REDOUBLANT, ABANDON, DIPLOME }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private User etudiant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_id", nullable = false)
    private Classe classe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annee_academique_id", nullable = false)
    private AnneeAcademique anneeAcademique;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false)
    
    private Statut statut = Statut.EN_COURS;

    @Column(name = "date_inscription", nullable = false)
    
    private LocalDateTime dateInscription = LocalDateTime.now();
    // ── Getters & Setters ──
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public User getEtudiant() { return this.etudiant; }
    public void setEtudiant(User etudiant) { this.etudiant = etudiant; }
    public Classe getClasse() { return this.classe; }
    public void setClasse(Classe classe) { this.classe = classe; }
    public AnneeAcademique getAnneeAcademique() { return this.anneeAcademique; }
    public void setAnneeAcademique(AnneeAcademique anneeAcademique) { this.anneeAcademique = anneeAcademique; }
    public Statut getStatut() { return this.statut; }
    public void setStatut(Statut statut) { this.statut = statut; }
    public LocalDateTime getDateInscription() { return this.dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }
    public Inscription() {}


    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static InscriptionBuilder builder() { return new InscriptionBuilder(); }

    public static class InscriptionBuilder {
        private Long id;
        private User etudiant;
        private Classe classe;
        private AnneeAcademique anneeAcademique;
        private Statut statut;
        private LocalDateTime dateInscription;

        public InscriptionBuilder id(Long v) { this.id = v; return this; }
        public InscriptionBuilder etudiant(User v) { this.etudiant = v; return this; }
        public InscriptionBuilder classe(Classe v) { this.classe = v; return this; }
        public InscriptionBuilder anneeAcademique(AnneeAcademique v) { this.anneeAcademique = v; return this; }
        public InscriptionBuilder statut(Statut v) { this.statut = v; return this; }
        public InscriptionBuilder dateInscription(LocalDateTime v) { this.dateInscription = v; return this; }

        public Inscription build() {
            Inscription obj = new Inscription();
            if (this.id != null) obj.id = this.id;
            if (this.etudiant != null) obj.etudiant = this.etudiant;
            if (this.classe != null) obj.classe = this.classe;
            if (this.anneeAcademique != null) obj.anneeAcademique = this.anneeAcademique;
            if (this.statut != null) obj.statut = this.statut;
            if (this.dateInscription != null) obj.dateInscription = this.dateInscription;
            return obj;
        }
    }
}
