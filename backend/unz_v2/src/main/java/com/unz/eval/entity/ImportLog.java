package com.unz.eval.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Trace persistante d'un import CSV de questions.
 * NOUVEAU (v3) : jusqu'ici le résultat de l'import était renvoyé une fois puis perdu.
 */
@Entity
@Table(name = "import_logs")
public class ImportLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matiere_id", nullable = false)
    private Matiere matiere;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "importe_par_id", nullable = false)
    private User importePar;

    @Column(name = "nb_importees", nullable = false)
    
    private int nbImportees = 0;

    @Column(name = "nb_erreurs", nullable = false)
    
    private int nbErreurs = 0;

    @Column(name = "date_import", nullable = false)
    
    private LocalDateTime dateImport = LocalDateTime.now();
    // ── Getters & Setters ──
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public Matiere getMatiere() { return this.matiere; }
    public void setMatiere(Matiere matiere) { this.matiere = matiere; }
    public User getImportePar() { return this.importePar; }
    public void setImportePar(User importePar) { this.importePar = importePar; }
    public int getNbImportees() { return this.nbImportees; }
    public void setNbImportees(int nbImportees) { this.nbImportees = nbImportees; }
    public int getNbErreurs() { return this.nbErreurs; }
    public void setNbErreurs(int nbErreurs) { this.nbErreurs = nbErreurs; }
    public LocalDateTime getDateImport() { return this.dateImport; }
    public void setDateImport(LocalDateTime dateImport) { this.dateImport = dateImport; }
    public ImportLog() {}


    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static ImportLogBuilder builder() { return new ImportLogBuilder(); }

    public static class ImportLogBuilder {
        private Long id;
        private Matiere matiere;
        private User importePar;
        private Integer nbImportees;
        private Integer nbErreurs;
        private LocalDateTime dateImport;

        public ImportLogBuilder id(Long v) { this.id = v; return this; }
        public ImportLogBuilder matiere(Matiere v) { this.matiere = v; return this; }
        public ImportLogBuilder importePar(User v) { this.importePar = v; return this; }
        public ImportLogBuilder nbImportees(Integer v) { this.nbImportees = v; return this; }
        public ImportLogBuilder nbErreurs(Integer v) { this.nbErreurs = v; return this; }
        public ImportLogBuilder dateImport(LocalDateTime v) { this.dateImport = v; return this; }

        public ImportLog build() {
            ImportLog obj = new ImportLog();
            if (this.id != null) obj.id = this.id;
            if (this.matiere != null) obj.matiere = this.matiere;
            if (this.importePar != null) obj.importePar = this.importePar;
            if (this.nbImportees != null) obj.nbImportees = this.nbImportees;
            if (this.nbErreurs != null) obj.nbErreurs = this.nbErreurs;
            if (this.dateImport != null) obj.dateImport = this.dateImport;
            return obj;
        }
    }
}
