package com.unz.eval.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.unz.eval.security.AesEncryptor;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    public enum Role { ADMIN, ENSEIGNANT, ETUDIANT }
    public enum StatutCompte { EN_ATTENTE, ACTIF, REJETE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 50)
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @Convert(converter = AesEncryptor.class)
    @Column(name = "first_name")
    private String firstName;

    @Convert(converter = AesEncryptor.class)
    @Column(name = "last_name")
    private String lastName;

    @Convert(converter = AesEncryptor.class)
    private String email;

    /**
     * Hachage SHA-256 déterministe de l'e-mail normalisé (minuscules, sans espaces).
     * Sert uniquement à la recherche exacte et à garantir l'unicité — le champ
     * `email` ci-dessus étant chiffré avec un IV aléatoire, il ne peut pas
     * servir à ça (deux chiffrements du même texte diffèrent toujours).
     * Recalculé automatiquement à chaque sauvegarde, jamais renseigné à la main.
     */
    @Column(name = "email_hash", length = 64)
    private String emailHash;

    @PrePersist
    @PreUpdate
    private void computeEmailHash() {
        this.emailHash = hashEmail(this.email);
    }

    /** Hachage SHA-256 d'un e-mail normalisé — non réversible, déterministe. */
    public static String hashEmail(String email) {
        if (email == null || email.isBlank()) return null;
        try {
            String normalise = email.trim().toLowerCase();
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(normalise.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }

    public String getEmailHash() { return this.emailHash; }

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "statut_compte", nullable = false, length = 20)
    private StatutCompte statutCompte = StatutCompte.ACTIF;

    @OneToMany(mappedBy = "etudiant", fetch = FetchType.LAZY)
    private List<Inscription> inscriptions;

    @OneToMany(mappedBy = "enseignant", fetch = FetchType.LAZY)
    private List<AffectationEnseignant> affectations;

    public User() {}

    // ── Builder manuel ────────────────────────────────────────────────────
    public static UserBuilder builder() { return new UserBuilder(); }

    public static class UserBuilder {
        private String username;
        private String password;
        private String firstName;
        private String lastName;
        private String email;
        private Role role;
        private boolean active = true;
        private StatutCompte statutCompte = StatutCompte.ACTIF;

        public UserBuilder username(String v) { this.username = v; return this; }
        public UserBuilder password(String v) { this.password = v; return this; }
        public UserBuilder firstName(String v) { this.firstName = v; return this; }
        public UserBuilder lastName(String v)  { this.lastName = v; return this; }
        public UserBuilder email(String v)     { this.email = v; return this; }
        public UserBuilder role(Role v)        { this.role = v; return this; }
        public UserBuilder active(boolean v)   { this.active = v; return this; }
        public UserBuilder statutCompte(StatutCompte v) { this.statutCompte = v; return this; }

        public User build() {
            User u = new User();
            u.username = this.username;
            u.password = this.password;
            u.firstName = this.firstName;
            u.lastName  = this.lastName;
            u.email     = this.email;
            u.role      = this.role;
            u.active    = this.active;
            u.statutCompte = this.statutCompte;
            return u;
        }
    }

    // ── Helpers rôle ─────────────────────────────────────────────────────
    public boolean isAdmin()      { return role == Role.ADMIN; }
    public boolean isEnseignant() { return role == Role.ENSEIGNANT; }
    public boolean isEtudiant()   { return role == Role.ETUDIANT; }

    public String getFullName() {
        if (firstName != null && lastName != null) return firstName + " " + lastName;
        return username;
    }

    public Inscription getInscriptionPour(AnneeAcademique annee) {
        if (inscriptions == null || annee == null) return null;
        return inscriptions.stream()
                .filter(i -> i.getAnneeAcademique().getId().equals(annee.getId()))
                .findFirst().orElse(null);
    }

    public Classe getClassePour(AnneeAcademique annee) {
        Inscription i = getInscriptionPour(annee);
        return i != null ? i.getClasse() : null;
    }

    // ── UserDetails ───────────────────────────────────────────────────────
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    @Override public String getUsername()               { return username; }
    @Override public String getPassword()               { return password; }
    @Override public boolean isAccountNonExpired()      { return true; }
    @Override public boolean isAccountNonLocked()       { return statutCompte != StatutCompte.EN_ATTENTE && statutCompte != StatutCompte.REJETE; }
    @Override public boolean isCredentialsNonExpired()  { return true; }
    @Override public boolean isEnabled()                { return active; }

    // ── Getters & Setters ─────────────────────────────────────────────────
    public Long getId()           { return id; }
    public void setId(Long id)    { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public String getFirstName()  { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName()   { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail()      { return email; }
    public void setEmail(String email) { this.email = email; }
    public Role getRole()         { return role; }
    public void setRole(Role role) { this.role = role; }
    public boolean isActive()     { return active; }
    public void setActive(boolean active) { this.active = active; }
    public StatutCompte getStatutCompte() { return statutCompte; }
    public void setStatutCompte(StatutCompte statutCompte) { this.statutCompte = statutCompte; }
    public List<Inscription> getInscriptions() { return inscriptions; }
    public void setInscriptions(List<Inscription> inscriptions) { this.inscriptions = inscriptions; }
    public List<AffectationEnseignant> getAffectations() { return affectations; }
    public void setAffectations(List<AffectationEnseignant> affectations) { this.affectations = affectations; }
}
