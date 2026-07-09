package com.unz.eval.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity @Table(name = "filieres")
public class Filiere {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true) private String nom;
    @Column(nullable = false, unique = true) private String code;

    @OneToMany(mappedBy = "filiere", cascade = CascadeType.ALL)
    private List<Niveau> niveaux;

    @Override public String toString() { return code + " - " + nom; }
    // ── Getters & Setters ──
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return this.nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getCode() { return this.code; }
    public void setCode(String code) { this.code = code; }
    public List<Niveau> getNiveaux() { return this.niveaux; }
    public void setNiveaux(List<Niveau> niveaux) { this.niveaux = niveaux; }
    public Filiere() {}


    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static FiliereBuilder builder() { return new FiliereBuilder(); }

    public static class FiliereBuilder {
        private Long id;
        private String nom;
        private String code;
        private List<Niveau> niveaux;

        public FiliereBuilder id(Long v) { this.id = v; return this; }
        public FiliereBuilder nom(String v) { this.nom = v; return this; }
        public FiliereBuilder code(String v) { this.code = v; return this; }
        public FiliereBuilder niveaux(List<Niveau> v) { this.niveaux = v; return this; }

        public Filiere build() {
            Filiere obj = new Filiere();
            if (this.id != null) obj.id = this.id;
            if (this.nom != null) obj.nom = this.nom;
            if (this.code != null) obj.code = this.code;
            if (this.niveaux != null) obj.niveaux = this.niveaux;
            return obj;
        }
    }
}
