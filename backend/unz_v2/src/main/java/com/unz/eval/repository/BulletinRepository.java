package com.unz.eval.repository;

import com.unz.eval.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BulletinRepository extends JpaRepository<Bulletin, Long> {
    // EF-23 : l'étudiant ne voit que les bulletins publiés
    List<Bulletin> findByEtudiantAndPublieTrue(User etudiant);
    // Admin voit tous les bulletins
    List<Bulletin> findByEtudiant(User etudiant);
    Optional<Bulletin> findByEtudiantAndSemestre(User etudiant, Semestre semestre);

    // Suppression définitive d'un utilisateur : vérifie s'il a des bulletins (comme étudiant ou comme générateur)
    boolean existsByEtudiant(User etudiant);
    boolean existsByGenerePar(User generePar);
}
