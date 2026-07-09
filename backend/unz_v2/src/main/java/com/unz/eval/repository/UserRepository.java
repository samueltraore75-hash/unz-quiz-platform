package com.unz.eval.repository;

import com.unz.eval.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmailHash(String emailHash);
    List<User> findAllByOrderByIdAsc();
    // ⚠️ Pas de findByEmail ici : le champ email est chiffré en base avec un IV
    // aléatoire (AesEncryptor), donc une recherche SQL directe (WHERE email = ?)
    // ne peut structurellement jamais matcher. Utiliser findByEmailHash à la
    // place (voir User.hashEmail()), ou AuthService.motDePasseOublie().
    boolean existsByUsername(String username);
    List<User> findByRole(User.Role role);
    List<User> findByStatutCompteOrderByIdDesc(User.StatutCompte statutCompte);

    /** v3 : passe par Inscription (remplace l'ancien findByClasse direct) */
    List<User> findByInscriptions_Classe(Classe classe);
}
