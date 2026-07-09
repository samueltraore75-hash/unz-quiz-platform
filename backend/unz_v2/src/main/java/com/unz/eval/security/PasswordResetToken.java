package com.unz.eval.security;

import com.unz.eval.entity.User;
import jakarta.persistence.*;

import java.time.Instant;

/**
 * Token de réinitialisation de mot de passe.
 * v3 (NOUVEAU) : jusqu'ici il n'existait aucun mécanisme de "mot de passe oublié".
 * À usage unique, expire après un délai court.
 */
@Entity
@Table(name = "password_reset_tokens")
    public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)

    private boolean used = false;

    public boolean isValid() {
        return !used && Instant.now().isBefore(expiresAt);
    }

    public PasswordResetToken() {}

    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getToken() { return this.token; }
    public void setToken(String token) { this.token = token; }
    public User getUser() { return this.user; }
    public void setUser(User user) { this.user = user; }
    public Instant getExpiresAt() { return this.expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public boolean isUsed() { return this.used; }
    public void setUsed(boolean used) { this.used = used; }
}
