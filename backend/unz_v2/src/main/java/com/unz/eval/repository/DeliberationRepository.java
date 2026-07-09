package com.unz.eval.repository;

import com.unz.eval.entity.Bulletin;
import com.unz.eval.entity.Deliberation;
import com.unz.eval.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliberationRepository extends JpaRepository<Deliberation, Long> {
    Optional<Deliberation> findByBulletin(Bulletin bulletin);

    // Suppression définitive d'un utilisateur : vérifie s'il a validé des délibérations
    boolean existsByValidePar(User validePar);
}
