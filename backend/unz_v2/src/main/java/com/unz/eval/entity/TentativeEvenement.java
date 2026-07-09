package com.unz.eval.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Événement suspect tracé pendant une tentative (anti-triche).
 * NOUVEAU (v3) : renforce ENF-1/ENF-9 pour un examen en ligne réel.
 */
@Entity
@Table(name = "tentative_evenements")
public class TentativeEvenement {

    public enum Type { PERTE_FOCUS, COPIER_COLLER, CHANGEMENT_ONGLET, RETOUR_FOCUS }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tentative_id", nullable = false)
    private Tentative tentative;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false)
    private Type type;

    @Column(nullable = false)
    
    private LocalDateTime horodatage = LocalDateTime.now();
    // ── Getters & Setters ──
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public Tentative getTentative() { return this.tentative; }
    public void setTentative(Tentative tentative) { this.tentative = tentative; }
    public Type getType() { return this.type; }
    public void setType(Type type) { this.type = type; }
    public LocalDateTime getHorodatage() { return this.horodatage; }
    public void setHorodatage(LocalDateTime horodatage) { this.horodatage = horodatage; }
    public TentativeEvenement() {}

}
