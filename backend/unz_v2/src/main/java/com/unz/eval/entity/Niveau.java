package com.unz.eval.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity @Table(name = "niveaux",
    uniqueConstraints = @UniqueConstraint(columnNames = {"libelle", "filiere_id"}))
public class Niveau {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String libelle;

    @ManyToOne(optional = false)
    @JoinColumn(name = "filiere_id", nullable = false)
    private Filiere filiere;

    @OneToMany(mappedBy = "niveau", cascade = CascadeType.ALL)
    private List<Classe> classes;

    @OneToMany(mappedBy = "niveau", cascade = CascadeType.ALL)
    private List<Semestre> semestres;

    @Override public String toString() { return libelle + " - " + filiere.getCode(); }
    // ── Getters & Setters ──
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getLibelle() { return this.libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public Filiere getFiliere() { return this.filiere; }
    public void setFiliere(Filiere filiere) { this.filiere = filiere; }
    public List<Classe> getClasses() { return this.classes; }
    public void setClasses(List<Classe> classes) { this.classes = classes; }
    public List<Semestre> getSemestres() { return this.semestres; }
    public void setSemestres(List<Semestre> semestres) { this.semestres = semestres; }
    public Niveau() {}


    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static NiveauBuilder builder() { return new NiveauBuilder(); }

    public static class NiveauBuilder {
        private Long id;
        private String libelle;
        private Filiere filiere;
        private List<Classe> classes;
        private List<Semestre> semestres;

        public NiveauBuilder id(Long v) { this.id = v; return this; }
        public NiveauBuilder libelle(String v) { this.libelle = v; return this; }
        public NiveauBuilder filiere(Filiere v) { this.filiere = v; return this; }
        public NiveauBuilder classes(List<Classe> v) { this.classes = v; return this; }
        public NiveauBuilder semestres(List<Semestre> v) { this.semestres = v; return this; }

        public Niveau build() {
            Niveau obj = new Niveau();
            if (this.id != null) obj.id = this.id;
            if (this.libelle != null) obj.libelle = this.libelle;
            if (this.filiere != null) obj.filiere = this.filiere;
            if (this.classes != null) obj.classes = this.classes;
            if (this.semestres != null) obj.semestres = this.semestres;
            return obj;
        }
    }
}
