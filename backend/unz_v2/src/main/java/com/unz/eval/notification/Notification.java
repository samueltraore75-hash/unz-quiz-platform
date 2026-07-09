package com.unz.eval.notification;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.unz.eval.entity.User;
import jakarta.persistence.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Notification pour un utilisateur.
 * Déclenchée automatiquement lors d'événements clés :
 *  - Quiz ouvert (étudiant)
 *  - Quiz clôturé / notes disponibles (étudiant)
 *  - Bulletin publié (étudiant)
 *  - Note manuelle saisie (étudiant)
 */
@Entity
@Table(name = "notifications")
public class Notification {

    public enum Type {
        QUIZ_OUVERT,
        QUIZ_CLOTURE,
        NOTES_DISPONIBLES,
        BULLETIN_PUBLIE,
        NOTE_SAISIE,
        DEMANDE_COMPTE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinataire_id", nullable = false)
    private User destinataire;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false)
    private Type type;

    @Column(nullable = false)
    private String titre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    
    private boolean lue = false;

    @Column(nullable = false)
    
    private LocalDateTime createdAt = LocalDateTime.now();

    private Long referenceId; // ID du quiz, bulletin, etc.

    public Notification() {}

    // ── Getters & Setters ──
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public User getDestinataire() { return this.destinataire; }
    public void setDestinataire(User destinataire) { this.destinataire = destinataire; }
    public Type getType() { return this.type; }
    public void setType(Type type) { this.type = type; }
    public String getTitre() { return this.titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getMessage() { return this.message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isLue() { return this.lue; }
    public void setLue(boolean lue) { this.lue = lue; }
    public LocalDateTime getCreatedAt() { return this.createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getReferenceId() { return this.referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }

    // ── Builder manuel ──────────────────────────────────────────────────
    public static NotificationBuilder builder() { return new NotificationBuilder(); }

    public static class NotificationBuilder {
        private User destinataire;
        private Type type;
        private String titre;
        private String message;
        private Long referenceId;

        public NotificationBuilder destinataire(User v) { this.destinataire = v; return this; }
        public NotificationBuilder type(Type v) { this.type = v; return this; }
        public NotificationBuilder titre(String v) { this.titre = v; return this; }
        public NotificationBuilder message(String v) { this.message = v; return this; }
        public NotificationBuilder referenceId(Long v) { this.referenceId = v; return this; }

        public Notification build() {
            Notification n = new Notification();
            n.destinataire = this.destinataire;
            n.type = this.type;
            n.titre = this.titre;
            n.message = this.message;
            n.referenceId = this.referenceId;
            return n;
        }
    }
}
