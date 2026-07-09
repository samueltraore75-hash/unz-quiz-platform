package com.unz.eval.security;

import com.unz.eval.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    public RefreshToken() {}

    // Builder pattern manuel
    public static RefreshTokenBuilder builder() { return new RefreshTokenBuilder(); }

    public static class RefreshTokenBuilder {
        private String token = UUID.randomUUID().toString();
        private User user;
        private LocalDateTime expiresAt;
        private boolean revoked = false;

        public RefreshTokenBuilder token(String token) { this.token = token; return this; }
        public RefreshTokenBuilder user(User user) { this.user = user; return this; }
        public RefreshTokenBuilder expiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; return this; }
        public RefreshTokenBuilder revoked(boolean revoked) { this.revoked = revoked; return this; }

        public RefreshToken build() {
            RefreshToken rt = new RefreshToken();
            rt.token = this.token;
            rt.user = this.user;
            rt.expiresAt = this.expiresAt;
            rt.revoked = this.revoked;
            return rt;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }

    /** Un token est valide s'il n'est pas révoqué et n'est pas expiré. */
    public boolean isValid() {
        return !revoked && expiresAt != null && expiresAt.isAfter(LocalDateTime.now());
    }
}
