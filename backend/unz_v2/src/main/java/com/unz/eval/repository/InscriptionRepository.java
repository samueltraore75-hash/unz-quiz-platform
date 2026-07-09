package com.unz.eval.repository;

import com.unz.eval.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InscriptionRepository extends JpaRepository<Inscription, Long> {
    // EF-2 : classe courante d'un étudiant pour une année académique donnée
    Optional<Inscription> findByEtudiantAndAnneeAcademique(User etudiant, AnneeAcademique annee);
    List<Inscription> findByEtudiantOrderByAnneeAcademique_DateDebutDesc(User etudiant);
    List<Inscription> findByClasse(Classe classe);

    // Suppression définitive d'un utilisateur : vérifie s'il a des inscriptions
    boolean existsByEtudiant(User etudiant);
}
