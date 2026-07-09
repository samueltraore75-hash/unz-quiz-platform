package com.unz.eval.audit;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.unz.eval.entity.User;
import jakarta.persistence.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

// ══════════════════════════════════════════════════════════════════════════
// ENTITÉ AuditLog
// ══════════════════════════════════════════════════════════════════════════

/**
 * Entrée de journal d'audit.
 * Trace : qui a fait quoi, quand, sur quel objet, avec quel résultat.
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(columnList = "username"),
    @Index(columnList = "action"),
    @Index(columnList = "timestamp")
})
    class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action; // LOGIN, CREATE_QUIZ, CLOTURE_QUIZ, GENERATE_BULLETIN...

    private String description;

    @Column(nullable = false)
    private String username; // qui a fait l'action

    private String role; // ADMIN, ENSEIGNANT, ETUDIANT

    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false)

    private Status status = Status.SUCCESS;

    private String errorMessage;

    @Column(nullable = false)

    private LocalDateTime timestamp = LocalDateTime.now();

    public enum Status { SUCCESS, FAILURE }

    public AuditLog() {}

    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getAction() { return this.action; }
    public void setAction(String action) { this.action = action; }
    public String getDescription() { return this.description; }
    public void setDescription(String description) { this.description = description; }
    public String getUsername() { return this.username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return this.role; }
    public void setRole(String role) { this.role = role; }
    public String getIpAddress() { return this.ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public Status getStatus() { return this.status; }
    public void setStatus(Status status) { this.status = status; }
    public String getErrorMessage() { return this.errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public LocalDateTime getTimestamp() { return this.timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    // ── Builder manuel (auto-généré) ──────────────────────────────────
    public static AuditLogBuilder builder() { return new AuditLogBuilder(); }

    public static class AuditLogBuilder {
        private Long id;
        private String action;
        private String description;
        private String username;
        private String role;
        private String ipAddress;
        private Status status;
        private String errorMessage;
        private LocalDateTime timestamp;

        public AuditLogBuilder id(Long v) { this.id = v; return this; }
        public AuditLogBuilder action(String v) { this.action = v; return this; }
        public AuditLogBuilder description(String v) { this.description = v; return this; }
        public AuditLogBuilder username(String v) { this.username = v; return this; }
        public AuditLogBuilder role(String v) { this.role = v; return this; }
        public AuditLogBuilder ipAddress(String v) { this.ipAddress = v; return this; }
        public AuditLogBuilder status(Status v) { this.status = v; return this; }
        public AuditLogBuilder errorMessage(String v) { this.errorMessage = v; return this; }
        public AuditLogBuilder timestamp(LocalDateTime v) { this.timestamp = v; return this; }

        public AuditLog build() {
            AuditLog obj = new AuditLog();
            if (this.id != null) obj.id = this.id;
            if (this.action != null) obj.action = this.action;
            if (this.description != null) obj.description = this.description;
            if (this.username != null) obj.username = this.username;
            if (this.role != null) obj.role = this.role;
            if (this.ipAddress != null) obj.ipAddress = this.ipAddress;
            if (this.status != null) obj.status = this.status;
            if (this.errorMessage != null) obj.errorMessage = this.errorMessage;
            if (this.timestamp != null) obj.timestamp = this.timestamp;
            return obj;
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// REPOSITORY
// ══════════════════════════════════════════════════════════════════════════

@Repository
interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUsernameOrderByTimestampDesc(String username);
    List<AuditLog> findByActionOrderByTimestampDesc(String action);
    List<AuditLog> findTop100ByOrderByTimestampDesc();
}

// ══════════════════════════════════════════════════════════════════════════
// ASPECT AOP — intercepte automatiquement les méthodes @Audited
// ══════════════════════════════════════════════════════════════════════════

/**
 * Aspect Spring AOP qui intercepte toutes les méthodes annotées @Audited.
 * Enregistre automatiquement l'action, l'utilisateur et le résultat (succès/échec).
 */
@Aspect
@Component
class AuditAspect {

    private final AuditLogRepository auditLogRepo;

    public AuditAspect(AuditLogRepository auditLogRepo) {
        this.auditLogRepo = auditLogRepo;
    }

    @Around("@annotation(audited)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {
        String username = "anonyme";
        String role = null;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User user) {
            username = user.getUsername();
            role = user.getRole().name();
        }

        AuditLog.AuditLogBuilder logBuilder = AuditLog.builder()
                .action(audited.action())
                .description(audited.description())
                .username(username)
                .role(role)
                .timestamp(LocalDateTime.now());

        try {
            Object result = joinPoint.proceed();
            logBuilder.status(AuditLog.Status.SUCCESS);
            auditLogRepo.save(logBuilder.build());
            return result;
        } catch (Exception e) {
            logBuilder.status(AuditLog.Status.FAILURE).errorMessage(e.getMessage());
            auditLogRepo.save(logBuilder.build());
            throw e;
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// SERVICE AUDIT — consulter les logs
// ══════════════════════════════════════════════════════════════════════════

/**
 * Service pour consulter les journaux d'audit (Admin uniquement).
 */
@Component
public class AuditService {

    private final AuditLogRepository auditLogRepo;

    public AuditService(AuditLogRepository auditLogRepo) {
        this.auditLogRepo = auditLogRepo;
    }

    public List<AuditLog> getDerniersLogs() {
        return auditLogRepo.findTop100ByOrderByTimestampDesc();
    }

    public List<AuditLog> getLogsParUtilisateur(String username) {
        return auditLogRepo.findByUsernameOrderByTimestampDesc(username);
    }

    public List<AuditLog> getLogsParAction(String action) {
        return auditLogRepo.findByActionOrderByTimestampDesc(action);
    }
}
