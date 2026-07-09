package com.unz.eval.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bulletin semestriel d'un étudiant.
 * EF-22 : généré par l'Administrateur
 * EF-23 : la publication est un acte EXPLICITE distinct du calcul
 * ENF-2 : un étudiant ne voit son bulletin qu'après publication (publie = true)
 * v3    : ajout de sessionExamen (NORMALE / RATTRAPAGE) et lien optionnel vers Deliberation
 */
@Entity
@Table(name = "bulletins",
       uniqueConstraints = @UniqueConstraint(columnNames = {"etudiant_id", "semestre_id", "session_examen"}))
public class Bulletin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private User etudiant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semestre_id", nullable = false)
    private Semestre semestre;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "session_examen", nullable = false)
    
    private SessionExamen sessionExamen = SessionExamen.NORMALE;

    @Column(name = "moyenne_generale", precision = 4, scale = 2)
    private BigDecimal moyenneGenerale;

    @Column(name = "credits_acquis", nullable = false)
    
    private Integer creditsAcquis = 0;

    @Column(name = "credits_total", nullable = false)
    
    private Integer creditsTotal = 0;

    @Column(name = "semestre_valide", nullable = false)
    
    private boolean semestreValide = false;

    /** EF-23 : false par défaut — publication explicite par l'Admin */
    @Column(nullable = false)
    
    private boolean publie = false;

    @Column(name = "date_publication")
    private LocalDateTime datePublication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genere_par_id")
    private User generePar;

    /** v3 : présente uniquement si une délibération de jury a eu lieu pour ce bulletin */
    @OneToOne(mappedBy = "bulletin", fetch = FetchType.LAZY)
    private Deliberation deliberation;
    // ── Getters & Setters ──
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public User getEtudiant() { return this.etudiant; }
    public void setEtudiant(User etudiant) { this.etudiant = etudiant; }
    public Semestre getSemestre() { return this.semestre; }
    public void setSemestre(Semestre semestre) { this.semestre = semestre; }
    public SessionExamen getSessionExamen() { return this.sessionExamen; }
    public void setSessionExamen(SessionExamen sessionExamen) { this.sessionExamen = sessionExamen; }
    public BigDecimal getMoyenneGenerale() { return this.moyenneGenerale; }
    public void setMoyenneGenerale(BigDecimal moyenneGenerale) { this.moyenneGenerale = moyenneGenerale; }
    public Integer getCreditsAcquis() { return this.creditsAcquis; }
    public void setCreditsAcquis(Integer creditsAcquis) { this.creditsAcquis = creditsAcquis; }
    public Integer getCreditsTotal() { return this.creditsTotal; }
    public void setCreditsTotal(Integer creditsTotal) { this.creditsTotal = creditsTotal; }
    public boolean isSemestreValide() { return this.semestreValide; }
    public void setSemestreValide(boolean semestreValide) { this.semestreValide = semestreValide; }
    public boolean isPublie() { return this.publie; }
    public void setPublie(boolean publie) { this.publie = publie; }
    public LocalDateTime getDatePublication() { return this.datePublication; }
    public void setDatePublication(LocalDateTime datePublication) { this.datePublication = datePublication; }
    public User getGenerePar() { return this.generePar; }
    public void setGenerePar(User generePar) { this.generePar = generePar; }
    public Deliberation getDeliberation() { return this.deliberation; }
    public void setDeliberation(Deliberation deliberation) { this.deliberation = deliberation; }
    public Bulletin() {}


    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static BulletinBuilder builder() { return new BulletinBuilder(); }

    public static class BulletinBuilder {
        private Long id;
        private User etudiant;
        private Semestre semestre;
        private SessionExamen sessionExamen;
        private BigDecimal moyenneGenerale;
        private Integer creditsAcquis;
        private Integer creditsTotal;
        private Boolean semestreValide;
        private Boolean publie;
        private LocalDateTime datePublication;
        private User generePar;
        private Deliberation deliberation;

        public BulletinBuilder id(Long v) { this.id = v; return this; }
        public BulletinBuilder etudiant(User v) { this.etudiant = v; return this; }
        public BulletinBuilder semestre(Semestre v) { this.semestre = v; return this; }
        public BulletinBuilder sessionExamen(SessionExamen v) { this.sessionExamen = v; return this; }
        public BulletinBuilder moyenneGenerale(BigDecimal v) { this.moyenneGenerale = v; return this; }
        public BulletinBuilder creditsAcquis(Integer v) { this.creditsAcquis = v; return this; }
        public BulletinBuilder creditsTotal(Integer v) { this.creditsTotal = v; return this; }
        public BulletinBuilder semestreValide(Boolean v) { this.semestreValide = v; return this; }
        public BulletinBuilder publie(Boolean v) { this.publie = v; return this; }
        public BulletinBuilder datePublication(LocalDateTime v) { this.datePublication = v; return this; }
        public BulletinBuilder generePar(User v) { this.generePar = v; return this; }
        public BulletinBuilder deliberation(Deliberation v) { this.deliberation = v; return this; }

        public Bulletin build() {
            Bulletin obj = new Bulletin();
            if (this.id != null) obj.id = this.id;
            if (this.etudiant != null) obj.etudiant = this.etudiant;
            if (this.semestre != null) obj.semestre = this.semestre;
            if (this.sessionExamen != null) obj.sessionExamen = this.sessionExamen;
            if (this.moyenneGenerale != null) obj.moyenneGenerale = this.moyenneGenerale;
            if (this.creditsAcquis != null) obj.creditsAcquis = this.creditsAcquis;
            if (this.creditsTotal != null) obj.creditsTotal = this.creditsTotal;
            if (this.semestreValide != null) obj.semestreValide = this.semestreValide;
            if (this.publie != null) obj.publie = this.publie;
            if (this.datePublication != null) obj.datePublication = this.datePublication;
            if (this.generePar != null) obj.generePar = this.generePar;
            if (this.deliberation != null) obj.deliberation = this.deliberation;
            return obj;
        }
    }
}
