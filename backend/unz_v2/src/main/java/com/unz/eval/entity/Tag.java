package com.unz.eval.entity;

import jakarta.persistence.*;

import java.util.List;

/**
 * Étiquette thématique pour organiser la banque de questions (chapitre, thème...).
 * NOUVEAU (v3).
 */
@Entity
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String libelle;

    @ManyToMany(mappedBy = "tags")
    private List<Question> questions;
    // ── Getters & Setters ──
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getLibelle() { return this.libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public List<Question> getQuestions() { return this.questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
    public Tag() {}

}
