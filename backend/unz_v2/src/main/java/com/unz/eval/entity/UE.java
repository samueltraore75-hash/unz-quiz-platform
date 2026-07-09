package com.unz.eval.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity @Table(name="ues")
public class UE {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private String nom;
    @Column(nullable=false) private Integer credits;
    @Column(nullable=false, precision=4, scale=2)
    private BigDecimal seuilValidation = BigDecimal.valueOf(10.00);
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="semestre_id", nullable=false) private Semestre semestre;
    @OneToMany(mappedBy="ue", cascade=CascadeType.ALL) private List<Matiere> matieres;
    // ── Getters & Setters ──
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return this.nom; }
    public void setNom(String nom) { this.nom = nom; }
    public Integer getCredits() { return this.credits; }
    public void setCredits(Integer credits) { this.credits = credits; }
    public BigDecimal getSeuilValidation() { return this.seuilValidation; }
    public void setSeuilValidation(BigDecimal seuilValidation) { this.seuilValidation = seuilValidation; }
    public Semestre getSemestre() { return this.semestre; }
    public void setSemestre(Semestre semestre) { this.semestre = semestre; }
    public List<Matiere> getMatieres() { return this.matieres; }
    public void setMatieres(List<Matiere> matieres) { this.matieres = matieres; }
    public UE() {}


    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static UEBuilder builder() { return new UEBuilder(); }

    public static class UEBuilder {
        private Long id;
        private String nom;
        private Integer credits;
        private BigDecimal seuilValidation;
        private Semestre semestre;
        private List<Matiere> matieres;

        public UEBuilder id(Long v) { this.id = v; return this; }
        public UEBuilder nom(String v) { this.nom = v; return this; }
        public UEBuilder credits(Integer v) { this.credits = v; return this; }
        public UEBuilder seuilValidation(BigDecimal v) { this.seuilValidation = v; return this; }
        public UEBuilder semestre(Semestre v) { this.semestre = v; return this; }
        public UEBuilder matieres(List<Matiere> v) { this.matieres = v; return this; }

        public UE build() {
            UE obj = new UE();
            if (this.id != null) obj.id = this.id;
            if (this.nom != null) obj.nom = this.nom;
            if (this.credits != null) obj.credits = this.credits;
            if (this.seuilValidation != null) obj.seuilValidation = this.seuilValidation;
            if (this.semestre != null) obj.semestre = this.semestre;
            if (this.matieres != null) obj.matieres = this.matieres;
            return obj;
        }
    }
}
