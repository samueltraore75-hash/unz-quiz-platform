package com.unz.eval.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Délibération de jury avant publication définitive d'un bulletin.
 * NOUVEAU (v3) : prépare la perspective d'évolution "délibération de jury".
 * Optionnelle : un Bulletin peut être publié sans Deliberation si le jury n'est pas requis.
 */
@Entity
@Table(name = "deliberations")
public class Deliberation {

    public enum Decision { VALIDE, AJOURNE, EXCLU }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bulletin_id", nullable = false, unique = true)
    private Bulletin bulletin;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false)
    private Decision decision;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "valide_par_id", nullable = false)
    private User validePar;

    @Column(name = "date_deliberation", nullable = false)
    
    private LocalDateTime dateDeliberation = LocalDateTime.now();
    // ── Getters & Setters ──
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public Bulletin getBulletin() { return this.bulletin; }
    public void setBulletin(Bulletin bulletin) { this.bulletin = bulletin; }
    public Decision getDecision() { return this.decision; }
    public void setDecision(Decision decision) { this.decision = decision; }
    public String getCommentaire() { return this.commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public User getValidePar() { return this.validePar; }
    public void setValidePar(User validePar) { this.validePar = validePar; }
    public LocalDateTime getDateDeliberation() { return this.dateDeliberation; }
    public void setDateDeliberation(LocalDateTime dateDeliberation) { this.dateDeliberation = dateDeliberation; }
    public Deliberation() {}

}
