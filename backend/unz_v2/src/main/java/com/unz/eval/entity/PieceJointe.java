package com.unz.eval.entity;

import jakarta.persistence.*;

/**
 * Image ou PDF illustrant une question (schéma, extrait de code, graphique...).
 * NOUVEAU (v3).
 */
@Entity
@Table(name = "pieces_jointes")
public class PieceJointe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(name = "type_mime", nullable = false, length = 50)
    private String typeMime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    // ── Getters & Setters ──
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getUrl() { return this.url; }
    public void setUrl(String url) { this.url = url; }
    public String getTypeMime() { return this.typeMime; }
    public void setTypeMime(String typeMime) { this.typeMime = typeMime; }
    public Question getQuestion() { return this.question; }
    public void setQuestion(Question question) { this.question = question; }
    public PieceJointe() {}

}
