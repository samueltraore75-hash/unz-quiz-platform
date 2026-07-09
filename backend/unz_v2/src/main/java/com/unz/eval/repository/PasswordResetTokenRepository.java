package com.unz.eval.repository;

import com.unz.eval.entity.User;
import com.unz.eval.security.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    // Suppression définitive d'un utilisateur : nettoie ses éventuels tokens de réinitialisation
    List<PasswordResetToken> findByUser(User user);
}
