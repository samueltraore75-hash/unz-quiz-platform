package com.unz.eval.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Note d'un étudiant dans une matière.
 * EF-17 : notes manuelles saisies par l'enseignant/admin
 * EF-18 : seuls les quiz de type EXAMEN génèrent une note officielle
 * EF-19 : la moyenne matière est calculée à partir de toutes les notes pondérées
 * v3    : ajout de sessionExamen (NORMALE / RATTRAPAGE)
 */
@Entity
@Table(name = "notes")
public class Note {

    public enum TypeNote { QUIZ, EXAMEN_ECRIT, TP, CONTROLE_CONTINU }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private User etudiant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matiere_id", nullable = false)
    private Matiere matiere;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "type_note", nullable = false)
    private TypeNote typeNote;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "session_examen", nullable = false)
    
    private SessionExamen sessionExamen = SessionExamen.NORMALE;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal valeur; // 0 à 20

    @Column(nullable = false, precision = 4, scale = 2)
    
    private BigDecimal ponderation = BigDecimal.ONE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saisie_par_id")
    private User saisieParr;

    @Column(name = "date_saisie", nullable = false)
    
    private LocalDateTime dateSaisie = LocalDateTime.now();
    // ── Getters & Setters ──
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public User getEtudiant() { return this.etudiant; }
    public void setEtudiant(User etudiant) { this.etudiant = etudiant; }
    public Matiere getMatiere() { return this.matiere; }
    public void setMatiere(Matiere matiere) { this.matiere = matiere; }
    public TypeNote getTypeNote() { return this.typeNote; }
    public void setTypeNote(TypeNote typeNote) { this.typeNote = typeNote; }
    public SessionExamen getSessionExamen() { return this.sessionExamen; }
    public void setSessionExamen(SessionExamen sessionExamen) { this.sessionExamen = sessionExamen; }
    public BigDecimal getValeur() { return this.valeur; }
    public void setValeur(BigDecimal valeur) { this.valeur = valeur; }
    public BigDecimal getPonderation() { return this.ponderation; }
    public void setPonderation(BigDecimal ponderation) { this.ponderation = ponderation; }
    public User getSaisieParr() { return this.saisieParr; }
    public void setSaisieParr(User saisieParr) { this.saisieParr = saisieParr; }
    public LocalDateTime getDateSaisie() { return this.dateSaisie; }
    public void setDateSaisie(LocalDateTime dateSaisie) { this.dateSaisie = dateSaisie; }
    public Note() {}


    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static NoteBuilder builder() { return new NoteBuilder(); }

    public static class NoteBuilder {
        private Long id;
        private User etudiant;
        private Matiere matiere;
        private TypeNote typeNote;
        private SessionExamen sessionExamen;
        private BigDecimal valeur;
        private BigDecimal ponderation;
        private User saisieParr;
        private LocalDateTime dateSaisie;

        public NoteBuilder id(Long v) { this.id = v; return this; }
        public NoteBuilder etudiant(User v) { this.etudiant = v; return this; }
        public NoteBuilder matiere(Matiere v) { this.matiere = v; return this; }
        public NoteBuilder typeNote(TypeNote v) { this.typeNote = v; return this; }
        public NoteBuilder sessionExamen(SessionExamen v) { this.sessionExamen = v; return this; }
        public NoteBuilder valeur(BigDecimal v) { this.valeur = v; return this; }
        public NoteBuilder ponderation(BigDecimal v) { this.ponderation = v; return this; }
        public NoteBuilder saisieParr(User v) { this.saisieParr = v; return this; }
        public NoteBuilder dateSaisie(LocalDateTime v) { this.dateSaisie = v; return this; }

        public Note build() {
            Note obj = new Note();
            if (this.id != null) obj.id = this.id;
            if (this.etudiant != null) obj.etudiant = this.etudiant;
            if (this.matiere != null) obj.matiere = this.matiere;
            if (this.typeNote != null) obj.typeNote = this.typeNote;
            if (this.sessionExamen != null) obj.sessionExamen = this.sessionExamen;
            if (this.valeur != null) obj.valeur = this.valeur;
            if (this.ponderation != null) obj.ponderation = this.ponderation;
            if (this.saisieParr != null) obj.saisieParr = this.saisieParr;
            if (this.dateSaisie != null) obj.dateSaisie = this.dateSaisie;
            return obj;
        }
    }
}
