package com.unz.eval.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "semestres",
    uniqueConstraints = @UniqueConstraint(columnNames = {"numero", "niveau_id", "annee_academique_id"}))
public class Semestre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer numero;

    @ManyToOne(optional = false)
    @JoinColumn(name = "niveau_id", nullable = false)
    private Niveau niveau;

    /** v3 : remplace l'ancien champ texte "anneeAcademique" */
    @ManyToOne(optional = false)
    @JoinColumn(name = "annee_academique_id", nullable = false)
    private AnneeAcademique anneeAcademique;

    @OneToMany(mappedBy = "semestre", cascade = CascadeType.ALL)
    private List<UE> ues;

    @Override
    public String toString() {
        return "S" + numero + " - " + niveau.toString() + " (" + anneeAcademique.getLibelle() + ")";
    }
    // ── Getters & Setters ──
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public Integer getNumero() { return this.numero; }
    public void setNumero(Integer numero) { this.numero = numero; }
    public Niveau getNiveau() { return this.niveau; }
    public void setNiveau(Niveau niveau) { this.niveau = niveau; }
    public AnneeAcademique getAnneeAcademique() { return this.anneeAcademique; }
    public void setAnneeAcademique(AnneeAcademique anneeAcademique) { this.anneeAcademique = anneeAcademique; }
    public List<UE> getUes() { return this.ues; }
    public void setUes(List<UE> ues) { this.ues = ues; }
    public Semestre() {}


    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static SemestreBuilder builder() { return new SemestreBuilder(); }

    public static class SemestreBuilder {
        private Long id;
        private Integer numero;
        private Niveau niveau;
        private AnneeAcademique anneeAcademique;
        private List<UE> ues;

        public SemestreBuilder id(Long v) { this.id = v; return this; }
        public SemestreBuilder numero(Integer v) { this.numero = v; return this; }
        public SemestreBuilder niveau(Niveau v) { this.niveau = v; return this; }
        public SemestreBuilder anneeAcademique(AnneeAcademique v) { this.anneeAcademique = v; return this; }
        public SemestreBuilder ues(List<UE> v) { this.ues = v; return this; }

        public Semestre build() {
            Semestre obj = new Semestre();
            if (this.id != null) obj.id = this.id;
            if (this.numero != null) obj.numero = this.numero;
            if (this.niveau != null) obj.niveau = this.niveau;
            if (this.anneeAcademique != null) obj.anneeAcademique = this.anneeAcademique;
            if (this.ues != null) obj.ues = this.ues;
            return obj;
        }
    }
}
